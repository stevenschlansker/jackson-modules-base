import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Emits N distinct top-level DTO records, each 2-3 levels deep with nested DTOs and a mix of
 * field types (primitive boolean/int/long, String, List<Nested>, enum). Distinct types matter:
 * each first-time serialize/deserialize forces fresh Blackbird accessor linkage (the LambdaForm /
 * MethodHandle linking that the suspected VM bug corrupts). Many distinct types first-touched
 * concurrently at cold start is the hypothesised trigger.
 *
 * Also emits DtoRegistry: id -> example instance + Class, so the controller can serve a distinct
 * type per /obj/{id} and the storm can POST/round-trip each one.
 *
 * Run: java DtoGenerator.java <outputDir>
 * (outputDir is the package root, e.g. src/main/java)
 */
public class DtoGenerator {

    static final int COUNT = 100;
    static final String PKG = "com.example.repro.dto";

    public static void main(String[] args) throws IOException {
        Path root = Path.of(args.length > 0 ? args[0] : "src/main/java");
        Path dir = root.resolve(PKG.replace('.', '/'));
        Files.createDirectories(dir);

        // A shared enum used by some DTOs.
        Files.writeString(dir.resolve("Color.java"),
                "package " + PKG + ";\n\npublic enum Color { RED, GREEN, BLUE, AMBER }\n",
                StandardCharsets.UTF_8);

        Random rnd = new Random(42); // deterministic generation
        List<String> registryEntries = new ArrayList<>();

        for (int i = 0; i < COUNT; i++) {
            // Each top-level DTO gets its own nested types so every class graph is distinct.
            String leafName = "LeafDto" + i;
            String midName = "MidDto" + i;
            String topName = "TopDto" + i;

            Files.writeString(dir.resolve(leafName + ".java"), leaf(leafName, i, rnd), StandardCharsets.UTF_8);
            Files.writeString(dir.resolve(midName + ".java"), mid(midName, leafName, i, rnd), StandardCharsets.UTF_8);
            Files.writeString(dir.resolve(topName + ".java"), top(topName, midName, leafName, i, rnd), StandardCharsets.UTF_8);

            registryEntries.add("        m.put(" + i + ", new Entry(" + topName + ".class, " + topName + ".sample()));");
        }

        // Reactive controller: one typed POST handler per DTO type. Taking the body as
        // Mono<TopDtoN> forces WebFlux's Jackson2JsonDecoder to deserialize the concrete type
        // reactively on the Netty event loop — which is the deserialization-on-event-loop path the
        // direct readValue() POST does NOT exercise. A single parameterized handler can't bind 100
        // concrete @RequestBody types, so we generate one handler each.
        writeReactiveController(dir);

        // Registry that exposes example instances and classes by id.
        StringBuilder reg = new StringBuilder();
        reg.append("package ").append(PKG).append(";\n\n");
        reg.append("import java.util.HashMap;\nimport java.util.Map;\n\n");
        reg.append("/** Generated. id -> (Class, sample instance) for all ").append(COUNT).append(" distinct top-level DTOs. */\n");
        reg.append("public final class DtoRegistry {\n");
        reg.append("    public static final int COUNT = ").append(COUNT).append(";\n\n");
        reg.append("    public record Entry(Class<?> type, Object sample) {}\n\n");
        reg.append("    private static final Map<Integer, Entry> BY_ID = build();\n\n");
        reg.append("    private static Map<Integer, Entry> build() {\n");
        reg.append("        Map<Integer, Entry> m = new HashMap<>();\n");
        for (String e : registryEntries) reg.append(e).append("\n");
        reg.append("        return m;\n");
        reg.append("    }\n\n");
        reg.append("    public static Entry get(int id) { return BY_ID.get(id); }\n");
        reg.append("    private DtoRegistry() {}\n");
        reg.append("}\n");
        Files.writeString(dir.resolve("DtoRegistry.java"), reg.toString(), StandardCharsets.UTF_8);

        System.out.println("Generated " + COUNT + " top-level DTOs (+ nested + enum + registry) into " + dir);
    }

    // Generated reactive controller with one typed POST handler per DTO type.
    static void writeReactiveController(Path dir) throws IOException {
        StringBuilder b = new StringBuilder();
        b.append("package ").append(PKG).append(";\n\n");
        b.append("import java.util.concurrent.atomic.AtomicBoolean;\n");
        b.append("import org.slf4j.Logger;\n");
        b.append("import org.slf4j.LoggerFactory;\n");
        b.append("import org.springframework.http.MediaType;\n");
        b.append("import org.springframework.web.bind.annotation.PostMapping;\n");
        b.append("import org.springframework.web.bind.annotation.RequestBody;\n");
        b.append("import org.springframework.web.bind.annotation.RestController;\n");
        b.append("import reactor.core.publisher.Mono;\n\n");
        b.append("/**\n");
        b.append(" * Generated. One typed reactive POST handler per distinct DTO type.\n");
        b.append(" *\n");
        b.append(" * Each handler takes its body as {@code Mono<TopDtoN>}, which forces WebFlux's\n");
        b.append(" * Jackson2JsonDecoder (built from the Blackbird-enabled primary ObjectMapper) to\n");
        b.append(" * deserialize the concrete type reactively on the Netty event loop. This is the\n");
        b.append(" * Blackbird CreatorOptimizer first-link-on-event-loop path the direct readValue()\n");
        b.append(" * POST in DtoController does not exercise. The handler logs the executing thread\n");
        b.append(" * once per type so the event-loop thread can be confirmed, then echoes the object.\n");
        b.append(" */\n");
        b.append("@RestController\n");
        b.append("public class ReactiveDtoController {\n\n");
        b.append("    private static final Logger log = LoggerFactory.getLogger(ReactiveDtoController.class);\n\n");
        b.append("    // Log the decode thread at most once per type to keep the cold-start storm log readable.\n");
        b.append("    private final AtomicBoolean[] logged = new AtomicBoolean[").append(COUNT).append("];\n");
        b.append("    { for (int i = 0; i < logged.length; i++) logged[i] = new AtomicBoolean(); }\n\n");
        for (int i = 0; i < COUNT; i++) {
            String topName = "TopDto" + i;
            b.append("    @PostMapping(value = \"/robj/").append(i).append("\",\n");
            b.append("            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)\n");
            b.append("    public Mono<").append(topName).append("> post").append(i)
                    .append("(@RequestBody Mono<").append(topName).append("> body) {\n");
            b.append("        return body.doOnNext(v -> {\n");
            b.append("            if (logged[").append(i).append("].compareAndSet(false, true)) {\n");
            b.append("                log.info(\"robj/").append(i)
                    .append(" decoded on thread {}\", Thread.currentThread().getName());\n");
            b.append("            }\n");
            b.append("        });\n");
            b.append("    }\n\n");
        }
        b.append("}\n");
        Files.writeString(dir.resolve("ReactiveDtoController.java"), b.toString(), StandardCharsets.UTF_8);
    }

    // Leaf: primitives + String + enum + boolean (the boolean drives the ToBooleanFunction accessor).
    static String leaf(String name, int i, Random rnd) {
        StringBuilder b = new StringBuilder();
        b.append("package ").append(PKG).append(";\n\n");
        b.append("public record ").append(name).append("(\n");
        b.append("        String label").append(i).append(",\n");
        b.append("        int count").append(i).append(",\n");
        b.append("        long size").append(i).append(",\n");
        b.append("        boolean active").append(i).append(",\n");      // -> ToBooleanFunction
        b.append("        boolean verified").append(i).append(",\n");    // second boolean
        b.append("        Color color").append(i).append("\n");
        b.append(") {\n");
        b.append("    public static ").append(name).append(" sample() {\n");
        b.append("        return new ").append(name).append("(\"leaf-").append(i).append("\", ")
                .append(rnd.nextInt(1000)).append(", ").append(rnd.nextInt(100000)).append("L, ")
                .append(rnd.nextBoolean()).append(", ").append(rnd.nextBoolean()).append(", Color.")
                .append(Color.values()[i % 4]).append(");\n");
        b.append("    }\n");
        b.append("}\n");
        return b.toString();
    }

    // Mid: holds a leaf + a List<leaf> + primitives.
    static String mid(String name, String leafName, int i, Random rnd) {
        StringBuilder b = new StringBuilder();
        b.append("package ").append(PKG).append(";\n\n");
        b.append("import java.util.List;\n\n");
        b.append("public record ").append(name).append("(\n");
        b.append("        ").append(leafName).append(" primary").append(i).append(",\n");
        b.append("        List<").append(leafName).append("> others").append(i).append(",\n");
        b.append("        int rank").append(i).append(",\n");
        b.append("        boolean enabled").append(i).append(",\n");    // -> ToBooleanFunction
        b.append("        String note").append(i).append("\n");
        b.append(") {\n");
        b.append("    public static ").append(name).append(" sample() {\n");
        b.append("        return new ").append(name).append("(\n");
        b.append("            ").append(leafName).append(".sample(),\n");
        b.append("            List.of(").append(leafName).append(".sample(), ").append(leafName).append(".sample()),\n");
        b.append("            ").append(rnd.nextInt(50)).append(", ").append(rnd.nextBoolean()).append(", \"mid-").append(i).append("\");\n");
        b.append("    }\n");
        b.append("}\n");
        return b.toString();
    }

    // Top: holds a mid + a List<mid> + a leaf + primitives. 3 levels deep overall.
    static String top(String name, String midName, String leafName, int i, Random rnd) {
        StringBuilder b = new StringBuilder();
        b.append("package ").append(PKG).append(";\n\n");
        b.append("import java.util.List;\n\n");
        b.append("public record ").append(name).append("(\n");
        b.append("        long id").append(i).append(",\n");
        b.append("        String name").append(i).append(",\n");
        b.append("        boolean flag").append(i).append(",\n");       // -> ToBooleanFunction
        b.append("        int score").append(i).append(",\n");
        b.append("        ").append(midName).append(" detail").append(i).append(",\n");
        b.append("        List<").append(midName).append("> children").append(i).append(",\n");
        b.append("        ").append(leafName).append(" extra").append(i).append("\n");
        b.append(") {\n");
        b.append("    public static ").append(name).append(" sample() {\n");
        b.append("        return new ").append(name).append("(\n");
        b.append("            ").append(rnd.nextInt(1_000_000)).append("L, \"top-").append(i).append("\", ")
                .append(rnd.nextBoolean()).append(", ").append(rnd.nextInt(1000)).append(",\n");
        b.append("            ").append(midName).append(".sample(),\n");
        b.append("            List.of(").append(midName).append(".sample(), ").append(midName).append(".sample()),\n");
        b.append("            ").append(leafName).append(".sample());\n");
        b.append("    }\n");
        b.append("}\n");
        return b.toString();
    }

    enum Color { RED, GREEN, BLUE, AMBER }
}
