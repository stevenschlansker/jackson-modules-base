package com.example.repro.dto;

import java.util.List;

public record TopDto77(
        long id77,
        String name77,
        boolean flag77,
        int score77,
        MidDto77 detail77,
        List<MidDto77> children77,
        LeafDto77 extra77
) {
    public static TopDto77 sample() {
        return new TopDto77(
            376138L, "top-77", true, 587,
            MidDto77.sample(),
            List.of(MidDto77.sample(), MidDto77.sample()),
            LeafDto77.sample());
    }
}
