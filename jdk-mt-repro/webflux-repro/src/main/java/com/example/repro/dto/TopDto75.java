package com.example.repro.dto;

import java.util.List;

public record TopDto75(
        long id75,
        String name75,
        boolean flag75,
        int score75,
        MidDto75 detail75,
        List<MidDto75> children75,
        LeafDto75 extra75
) {
    public static TopDto75 sample() {
        return new TopDto75(
            171061L, "top-75", true, 999,
            MidDto75.sample(),
            List.of(MidDto75.sample(), MidDto75.sample()),
            LeafDto75.sample());
    }
}
