package com.example.repro.dto;

import java.util.List;

public record TopDto74(
        long id74,
        String name74,
        boolean flag74,
        int score74,
        MidDto74 detail74,
        List<MidDto74> children74,
        LeafDto74 extra74
) {
    public static TopDto74 sample() {
        return new TopDto74(
            556190L, "top-74", true, 111,
            MidDto74.sample(),
            List.of(MidDto74.sample(), MidDto74.sample()),
            LeafDto74.sample());
    }
}
