package com.example.repro.dto;

import java.util.List;

public record TopDto12(
        long id12,
        String name12,
        boolean flag12,
        int score12,
        MidDto12 detail12,
        List<MidDto12> children12,
        LeafDto12 extra12
) {
    public static TopDto12 sample() {
        return new TopDto12(
            294031L, "top-12", false, 806,
            MidDto12.sample(),
            List.of(MidDto12.sample(), MidDto12.sample()),
            LeafDto12.sample());
    }
}
