package com.example.repro;

import com.example.repro.dto.DtoRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Serves a distinct DTO type per id so that a concurrent burst across all ids first-touches ~100
 * distinct serialize/deserialize accessor chains at once — the hypothesised trigger for the
 * Blackbird MethodHandle linkage corruption.
 *
 * GET  /obj/{id}  -> serialize a sample instance of distinct type id   (Blackbird serializer path)
 * POST /obj/{id}  -> deserialize a JSON body into distinct type id     (Blackbird CreatorOptimizer path)
 */
@RestController
public class DtoController {

    private final ObjectMapper objectMapper;

    public DtoController(JacksonConfig.ObjectMapperHolder holder) {
        // The Blackbird-enabled, auto-configured primary ObjectMapper (same instance the WebFlux
        // codecs are built from).
        this.objectMapper = holder.objectMapper();
    }

    /** Serialization path: returns a Mono of the distinct DTO type for {id}. */
    @GetMapping(value = "/obj/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Object> get(@PathVariable int id) {
        DtoRegistry.Entry entry = DtoRegistry.get(id % DtoRegistry.COUNT);
        return Mono.just(entry.sample());
    }

    /**
     * Deserialization path: reads the JSON body into the distinct DTO type for {id} using the
     * Blackbird-enabled mapper, exercising Blackbird's CreatorOptimizer / (MethodHandle)Function
     * linkage, then re-serializes via the WebFlux encoder.
     *
     * The body is taken as a raw String so a single handler can target all 100 distinct types; the
     * actual Blackbird deserialization happens in readValue against the concrete type.
     */
    @PostMapping(value = "/obj/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Object> post(@PathVariable int id, @RequestBody String body) {
        DtoRegistry.Entry entry = DtoRegistry.get(id % DtoRegistry.COUNT);
        return Mono.fromCallable(() -> objectMapper.readValue(body, entry.type()));
    }

    /** Convenience: the canonical JSON for type {id}, used by the storm to build POST bodies. */
    @GetMapping(value = "/sample/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Object> sample(@PathVariable int id) {
        return get(id);
    }

    /**
     * Readiness probe that returns a plain String, NOT a DTO. The storm uses this to detect the
     * port is open without first-touching any DTO serialize path, so the subsequent burst is the
     * true first traffic for all distinct types.
     */
    @GetMapping(value = "/ready", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> ready() {
        return Mono.just("ok");
    }
}
