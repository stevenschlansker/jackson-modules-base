package com.example.repro.dto;

import java.util.List;

public record TopDto22(
        long id22,
        String name22,
        boolean flag22,
        int score22,
        MidDto22 detail22,
        List<MidDto22> children22,
        LeafDto22 extra22
) {
    public static TopDto22 sample() {
        return new TopDto22(
            401269L, "top-22", true, 122,
            MidDto22.sample(),
            List.of(MidDto22.sample(), MidDto22.sample()),
            LeafDto22.sample());
    }
}
