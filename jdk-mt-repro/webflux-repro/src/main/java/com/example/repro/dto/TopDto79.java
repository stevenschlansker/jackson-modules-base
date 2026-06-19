package com.example.repro.dto;

import java.util.List;

public record TopDto79(
        long id79,
        String name79,
        boolean flag79,
        int score79,
        MidDto79 detail79,
        List<MidDto79> children79,
        LeafDto79 extra79
) {
    public static TopDto79 sample() {
        return new TopDto79(
            280550L, "top-79", true, 281,
            MidDto79.sample(),
            List.of(MidDto79.sample(), MidDto79.sample()),
            LeafDto79.sample());
    }
}
