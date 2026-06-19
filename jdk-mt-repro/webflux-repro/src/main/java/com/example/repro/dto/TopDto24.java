package com.example.repro.dto;

import java.util.List;

public record TopDto24(
        long id24,
        String name24,
        boolean flag24,
        int score24,
        MidDto24 detail24,
        List<MidDto24> children24,
        LeafDto24 extra24
) {
    public static TopDto24 sample() {
        return new TopDto24(
            571511L, "top-24", false, 673,
            MidDto24.sample(),
            List.of(MidDto24.sample(), MidDto24.sample()),
            LeafDto24.sample());
    }
}
