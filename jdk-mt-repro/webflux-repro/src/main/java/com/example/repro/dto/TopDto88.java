package com.example.repro.dto;

import java.util.List;

public record TopDto88(
        long id88,
        String name88,
        boolean flag88,
        int score88,
        MidDto88 detail88,
        List<MidDto88> children88,
        LeafDto88 extra88
) {
    public static TopDto88 sample() {
        return new TopDto88(
            185730L, "top-88", false, 505,
            MidDto88.sample(),
            List.of(MidDto88.sample(), MidDto88.sample()),
            LeafDto88.sample());
    }
}
