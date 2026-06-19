package com.example.repro.dto;

import java.util.List;

public record TopDto59(
        long id59,
        String name59,
        boolean flag59,
        int score59,
        MidDto59 detail59,
        List<MidDto59> children59,
        LeafDto59 extra59
) {
    public static TopDto59 sample() {
        return new TopDto59(
            283775L, "top-59", false, 411,
            MidDto59.sample(),
            List.of(MidDto59.sample(), MidDto59.sample()),
            LeafDto59.sample());
    }
}
