package com.example.repro.dto;

import java.util.List;

public record TopDto36(
        long id36,
        String name36,
        boolean flag36,
        int score36,
        MidDto36 detail36,
        List<MidDto36> children36,
        LeafDto36 extra36
) {
    public static TopDto36 sample() {
        return new TopDto36(
            841248L, "top-36", false, 741,
            MidDto36.sample(),
            List.of(MidDto36.sample(), MidDto36.sample()),
            LeafDto36.sample());
    }
}
