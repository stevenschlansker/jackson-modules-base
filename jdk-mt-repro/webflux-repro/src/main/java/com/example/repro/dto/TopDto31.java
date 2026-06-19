package com.example.repro.dto;

import java.util.List;

public record TopDto31(
        long id31,
        String name31,
        boolean flag31,
        int score31,
        MidDto31 detail31,
        List<MidDto31> children31,
        LeafDto31 extra31
) {
    public static TopDto31 sample() {
        return new TopDto31(
            902866L, "top-31", true, 788,
            MidDto31.sample(),
            List.of(MidDto31.sample(), MidDto31.sample()),
            LeafDto31.sample());
    }
}
