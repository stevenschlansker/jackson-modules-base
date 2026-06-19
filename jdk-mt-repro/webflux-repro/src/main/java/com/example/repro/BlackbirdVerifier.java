package com.example.repro;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Confirms at startup that the JSON encoder WebFlux will actually use for response bodies has the
 * Blackbird module registered. The point of the repro is the Blackbird code path; if Blackbird is
 * not wired in, the app exercises the wrong code, so this fails the boot loudly rather than letting
 * a misconfigured run masquerade as a real one.
 */
@Component
public class BlackbirdVerifier {

    private static final Logger log = LoggerFactory.getLogger(BlackbirdVerifier.class);

    private final ServerCodecConfigurer codecConfigurer;

    public BlackbirdVerifier(ServerCodecConfigurer codecConfigurer) {
        this.codecConfigurer = codecConfigurer;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verify() {
        ObjectMapper encoderMapper = findJacksonEncoderMapper();
        if (encoderMapper == null) {
            throw new IllegalStateException(
                    "No Jackson2JsonEncoder found among WebFlux server codecs; cannot confirm Blackbird path.");
        }
        Set<Object> moduleIds = encoderMapper.getRegisteredModuleIds();
        boolean blackbird = moduleIds.stream().anyMatch(id -> String.valueOf(id).toLowerCase().contains("blackbird"));
        log.info("WebFlux Jackson encoder registered modules: {}", moduleIds);
        if (!blackbird) {
            throw new IllegalStateException(
                    "Blackbird module is NOT registered on the WebFlux JSON encoder. Registered: " + moduleIds);
        }
        log.info("CONFIRMED: Blackbird is on the WebFlux JSON encoder path.");
    }

    private ObjectMapper findJacksonEncoderMapper() {
        for (HttpMessageWriter<?> writer : codecConfigurer.getWriters()) {
            if (writer instanceof EncoderHttpMessageWriter<?> ehmw
                    && ehmw.getEncoder() instanceof Jackson2JsonEncoder jackson) {
                return jackson.getObjectMapper();
            }
        }
        return null;
    }
}
