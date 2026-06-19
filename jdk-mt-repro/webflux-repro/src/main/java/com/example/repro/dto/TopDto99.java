package com.example.repro.dto;

import java.util.List;

public record TopDto99(
        long id99,
        String name99,
        boolean flag99,
        int score99,
        MidDto99 detail99,
        List<MidDto99> children99,
        LeafDto99 extra99
) {
    public static TopDto99 sample() {
        return new TopDto99(
            216648L, "top-99", true, 121,
            MidDto99.sample(),
            List.of(MidDto99.sample(), MidDto99.sample()),
            LeafDto99.sample());
    }
}
