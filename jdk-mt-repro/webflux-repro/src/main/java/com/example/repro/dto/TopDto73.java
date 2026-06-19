package com.example.repro.dto;

import java.util.List;

public record TopDto73(
        long id73,
        String name73,
        boolean flag73,
        int score73,
        MidDto73 detail73,
        List<MidDto73> children73,
        LeafDto73 extra73
) {
    public static TopDto73 sample() {
        return new TopDto73(
            750729L, "top-73", true, 267,
            MidDto73.sample(),
            List.of(MidDto73.sample(), MidDto73.sample()),
            LeafDto73.sample());
    }
}
