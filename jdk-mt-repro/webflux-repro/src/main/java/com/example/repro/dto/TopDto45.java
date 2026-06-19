package com.example.repro.dto;

import java.util.List;

public record TopDto45(
        long id45,
        String name45,
        boolean flag45,
        int score45,
        MidDto45 detail45,
        List<MidDto45> children45,
        LeafDto45 extra45
) {
    public static TopDto45 sample() {
        return new TopDto45(
            932431L, "top-45", true, 688,
            MidDto45.sample(),
            List.of(MidDto45.sample(), MidDto45.sample()),
            LeafDto45.sample());
    }
}
