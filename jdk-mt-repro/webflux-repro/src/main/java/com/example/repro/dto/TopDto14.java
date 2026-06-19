package com.example.repro.dto;

import java.util.List;

public record TopDto14(
        long id14,
        String name14,
        boolean flag14,
        int score14,
        MidDto14 detail14,
        List<MidDto14> children14,
        LeafDto14 extra14
) {
    public static TopDto14 sample() {
        return new TopDto14(
            226238L, "top-14", true, 672,
            MidDto14.sample(),
            List.of(MidDto14.sample(), MidDto14.sample()),
            LeafDto14.sample());
    }
}
