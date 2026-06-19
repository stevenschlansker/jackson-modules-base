package com.example.repro.dto;

import java.util.List;

public record TopDto8(
        long id8,
        String name8,
        boolean flag8,
        int score8,
        MidDto8 detail8,
        List<MidDto8> children8,
        LeafDto8 extra8
) {
    public static TopDto8 sample() {
        return new TopDto8(
            90625L, "top-8", true, 200,
            MidDto8.sample(),
            List.of(MidDto8.sample(), MidDto8.sample()),
            LeafDto8.sample());
    }
}
