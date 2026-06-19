package com.example.repro.dto;

import java.util.List;

public record TopDto46(
        long id46,
        String name46,
        boolean flag46,
        int score46,
        MidDto46 detail46,
        List<MidDto46> children46,
        LeafDto46 extra46
) {
    public static TopDto46 sample() {
        return new TopDto46(
            286249L, "top-46", false, 193,
            MidDto46.sample(),
            List.of(MidDto46.sample(), MidDto46.sample()),
            LeafDto46.sample());
    }
}
