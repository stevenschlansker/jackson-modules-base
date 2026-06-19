package com.example.repro.dto;

import java.util.List;

public record TopDto13(
        long id13,
        String name13,
        boolean flag13,
        int score13,
        MidDto13 detail13,
        List<MidDto13> children13,
        LeafDto13 extra13
) {
    public static TopDto13 sample() {
        return new TopDto13(
            135323L, "top-13", false, 332,
            MidDto13.sample(),
            List.of(MidDto13.sample(), MidDto13.sample()),
            LeafDto13.sample());
    }
}
