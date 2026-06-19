package com.example.repro.dto;

import java.util.List;

public record TopDto27(
        long id27,
        String name27,
        boolean flag27,
        int score27,
        MidDto27 detail27,
        List<MidDto27> children27,
        LeafDto27 extra27
) {
    public static TopDto27 sample() {
        return new TopDto27(
            312675L, "top-27", true, 245,
            MidDto27.sample(),
            List.of(MidDto27.sample(), MidDto27.sample()),
            LeafDto27.sample());
    }
}
