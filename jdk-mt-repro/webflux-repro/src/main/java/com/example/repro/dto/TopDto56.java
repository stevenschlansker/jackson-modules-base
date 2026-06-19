package com.example.repro.dto;

import java.util.List;

public record TopDto56(
        long id56,
        String name56,
        boolean flag56,
        int score56,
        MidDto56 detail56,
        List<MidDto56> children56,
        LeafDto56 extra56
) {
    public static TopDto56 sample() {
        return new TopDto56(
            951819L, "top-56", false, 174,
            MidDto56.sample(),
            List.of(MidDto56.sample(), MidDto56.sample()),
            LeafDto56.sample());
    }
}
