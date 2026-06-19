package com.example.repro.dto;

import java.util.List;

public record TopDto26(
        long id26,
        String name26,
        boolean flag26,
        int score26,
        MidDto26 detail26,
        List<MidDto26> children26,
        LeafDto26 extra26
) {
    public static TopDto26 sample() {
        return new TopDto26(
            702386L, "top-26", true, 431,
            MidDto26.sample(),
            List.of(MidDto26.sample(), MidDto26.sample()),
            LeafDto26.sample());
    }
}
