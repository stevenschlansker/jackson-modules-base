package com.example.repro.dto;

import java.util.List;

public record TopDto20(
        long id20,
        String name20,
        boolean flag20,
        int score20,
        MidDto20 detail20,
        List<MidDto20> children20,
        LeafDto20 extra20
) {
    public static TopDto20 sample() {
        return new TopDto20(
            204536L, "top-20", true, 492,
            MidDto20.sample(),
            List.of(MidDto20.sample(), MidDto20.sample()),
            LeafDto20.sample());
    }
}
