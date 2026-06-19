package com.example.repro.dto;

import java.util.List;

public record TopDto62(
        long id62,
        String name62,
        boolean flag62,
        int score62,
        MidDto62 detail62,
        List<MidDto62> children62,
        LeafDto62 extra62
) {
    public static TopDto62 sample() {
        return new TopDto62(
            12337L, "top-62", false, 588,
            MidDto62.sample(),
            List.of(MidDto62.sample(), MidDto62.sample()),
            LeafDto62.sample());
    }
}
