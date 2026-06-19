package com.example.repro.dto;

import java.util.List;

public record TopDto49(
        long id49,
        String name49,
        boolean flag49,
        int score49,
        MidDto49 detail49,
        List<MidDto49> children49,
        LeafDto49 extra49
) {
    public static TopDto49 sample() {
        return new TopDto49(
            566201L, "top-49", true, 402,
            MidDto49.sample(),
            List.of(MidDto49.sample(), MidDto49.sample()),
            LeafDto49.sample());
    }
}
