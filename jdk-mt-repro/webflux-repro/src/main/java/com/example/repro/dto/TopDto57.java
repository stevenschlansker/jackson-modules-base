package com.example.repro.dto;

import java.util.List;

public record TopDto57(
        long id57,
        String name57,
        boolean flag57,
        int score57,
        MidDto57 detail57,
        List<MidDto57> children57,
        LeafDto57 extra57
) {
    public static TopDto57 sample() {
        return new TopDto57(
            109875L, "top-57", true, 834,
            MidDto57.sample(),
            List.of(MidDto57.sample(), MidDto57.sample()),
            LeafDto57.sample());
    }
}
