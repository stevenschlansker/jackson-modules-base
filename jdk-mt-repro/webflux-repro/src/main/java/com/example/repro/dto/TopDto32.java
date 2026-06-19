package com.example.repro.dto;

import java.util.List;

public record TopDto32(
        long id32,
        String name32,
        boolean flag32,
        int score32,
        MidDto32 detail32,
        List<MidDto32> children32,
        LeafDto32 extra32
) {
    public static TopDto32 sample() {
        return new TopDto32(
            814507L, "top-32", false, 963,
            MidDto32.sample(),
            List.of(MidDto32.sample(), MidDto32.sample()),
            LeafDto32.sample());
    }
}
