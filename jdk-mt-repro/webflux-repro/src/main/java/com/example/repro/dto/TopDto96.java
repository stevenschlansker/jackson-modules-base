package com.example.repro.dto;

import java.util.List;

public record TopDto96(
        long id96,
        String name96,
        boolean flag96,
        int score96,
        MidDto96 detail96,
        List<MidDto96> children96,
        LeafDto96 extra96
) {
    public static TopDto96 sample() {
        return new TopDto96(
            30980L, "top-96", false, 929,
            MidDto96.sample(),
            List.of(MidDto96.sample(), MidDto96.sample()),
            LeafDto96.sample());
    }
}
