package com.example.repro.dto;

import java.util.List;

public record TopDto90(
        long id90,
        String name90,
        boolean flag90,
        int score90,
        MidDto90 detail90,
        List<MidDto90> children90,
        LeafDto90 extra90
) {
    public static TopDto90 sample() {
        return new TopDto90(
            111682L, "top-90", false, 206,
            MidDto90.sample(),
            List.of(MidDto90.sample(), MidDto90.sample()),
            LeafDto90.sample());
    }
}
