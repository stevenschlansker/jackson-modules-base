package com.example.repro.dto;

import java.util.List;

public record TopDto78(
        long id78,
        String name78,
        boolean flag78,
        int score78,
        MidDto78 detail78,
        List<MidDto78> children78,
        LeafDto78 extra78
) {
    public static TopDto78 sample() {
        return new TopDto78(
            297556L, "top-78", true, 859,
            MidDto78.sample(),
            List.of(MidDto78.sample(), MidDto78.sample()),
            LeafDto78.sample());
    }
}
