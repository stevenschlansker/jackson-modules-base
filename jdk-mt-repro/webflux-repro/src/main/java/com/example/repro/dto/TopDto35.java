package com.example.repro.dto;

import java.util.List;

public record TopDto35(
        long id35,
        String name35,
        boolean flag35,
        int score35,
        MidDto35 detail35,
        List<MidDto35> children35,
        LeafDto35 extra35
) {
    public static TopDto35 sample() {
        return new TopDto35(
            545005L, "top-35", false, 509,
            MidDto35.sample(),
            List.of(MidDto35.sample(), MidDto35.sample()),
            LeafDto35.sample());
    }
}
