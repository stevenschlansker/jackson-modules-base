package com.example.repro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Forces Blackbird onto the WebFlux JSON codec path.
 *
 * Spring Boot's JacksonAutoConfiguration applies every Jackson2ObjectMapperBuilderCustomizer to the
 * primary ObjectMapper, and WebFlux's default Jackson2JsonEncoder/Decoder are built from that same
 * primary ObjectMapper. Registering BlackbirdModule here therefore puts Blackbird on the
 * encode/decode path for every Mono&lt;Dto&gt; response and every @RequestBody deserialization.
 *
 * Runtime confirmation that Blackbird is genuinely on the path is done by {@link BlackbirdVerifier},
 * which inspects the actual server codecs at startup.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer blackbirdCustomizer() {
        return builder -> builder.modulesToInstall(new BlackbirdModule());
    }

    /** Exposes the auto-configured primary ObjectMapper for the verifier and POST round-trips. */
    @Bean
    public ObjectMapperHolder objectMapperHolder(ObjectMapper objectMapper) {
        return new ObjectMapperHolder(objectMapper);
    }

    public record ObjectMapperHolder(ObjectMapper objectMapper) {}
}
