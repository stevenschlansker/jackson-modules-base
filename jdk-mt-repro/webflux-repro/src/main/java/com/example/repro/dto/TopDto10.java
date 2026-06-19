package com.example.repro.dto;

import java.util.List;

public record TopDto10(
        long id10,
        String name10,
        boolean flag10,
        int score10,
        MidDto10 detail10,
        List<MidDto10> children10,
        LeafDto10 extra10
) {
    public static TopDto10 sample() {
        return new TopDto10(
            48229L, "top-10", false, 476,
            MidDto10.sample(),
            List.of(MidDto10.sample(), MidDto10.sample()),
            LeafDto10.sample());
    }
}
