package com.example.repro.dto;

import java.util.List;

public record TopDto40(
        long id40,
        String name40,
        boolean flag40,
        int score40,
        MidDto40 detail40,
        List<MidDto40> children40,
        LeafDto40 extra40
) {
    public static TopDto40 sample() {
        return new TopDto40(
            372765L, "top-40", false, 596,
            MidDto40.sample(),
            List.of(MidDto40.sample(), MidDto40.sample()),
            LeafDto40.sample());
    }
}
