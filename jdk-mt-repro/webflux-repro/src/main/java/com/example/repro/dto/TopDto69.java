package com.example.repro.dto;

import java.util.List;

public record TopDto69(
        long id69,
        String name69,
        boolean flag69,
        int score69,
        MidDto69 detail69,
        List<MidDto69> children69,
        LeafDto69 extra69
) {
    public static TopDto69 sample() {
        return new TopDto69(
            842790L, "top-69", true, 137,
            MidDto69.sample(),
            List.of(MidDto69.sample(), MidDto69.sample()),
            LeafDto69.sample());
    }
}
