package com.example.repro.dto;

import java.util.List;

public record TopDto85(
        long id85,
        String name85,
        boolean flag85,
        int score85,
        MidDto85 detail85,
        List<MidDto85> children85,
        LeafDto85 extra85
) {
    public static TopDto85 sample() {
        return new TopDto85(
            802601L, "top-85", false, 755,
            MidDto85.sample(),
            List.of(MidDto85.sample(), MidDto85.sample()),
            LeafDto85.sample());
    }
}
