package com.example.repro.dto;

import java.util.List;

public record TopDto16(
        long id16,
        String name16,
        boolean flag16,
        int score16,
        MidDto16 detail16,
        List<MidDto16> children16,
        LeafDto16 extra16
) {
    public static TopDto16 sample() {
        return new TopDto16(
            901630L, "top-16", false, 21,
            MidDto16.sample(),
            List.of(MidDto16.sample(), MidDto16.sample()),
            LeafDto16.sample());
    }
}
