package com.example.repro.dto;

import java.util.List;

public record TopDto23(
        long id23,
        String name23,
        boolean flag23,
        int score23,
        MidDto23 detail23,
        List<MidDto23> children23,
        LeafDto23 extra23
) {
    public static TopDto23 sample() {
        return new TopDto23(
            881948L, "top-23", true, 305,
            MidDto23.sample(),
            List.of(MidDto23.sample(), MidDto23.sample()),
            LeafDto23.sample());
    }
}
