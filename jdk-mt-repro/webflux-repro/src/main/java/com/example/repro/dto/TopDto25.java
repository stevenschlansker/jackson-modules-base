package com.example.repro.dto;

import java.util.List;

public record TopDto25(
        long id25,
        String name25,
        boolean flag25,
        int score25,
        MidDto25 detail25,
        List<MidDto25> children25,
        LeafDto25 extra25
) {
    public static TopDto25 sample() {
        return new TopDto25(
            546417L, "top-25", true, 347,
            MidDto25.sample(),
            List.of(MidDto25.sample(), MidDto25.sample()),
            LeafDto25.sample());
    }
}
