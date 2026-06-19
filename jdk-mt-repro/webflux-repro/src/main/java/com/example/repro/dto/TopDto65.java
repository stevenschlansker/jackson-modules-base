package com.example.repro.dto;

import java.util.List;

public record TopDto65(
        long id65,
        String name65,
        boolean flag65,
        int score65,
        MidDto65 detail65,
        List<MidDto65> children65,
        LeafDto65 extra65
) {
    public static TopDto65 sample() {
        return new TopDto65(
            651645L, "top-65", true, 443,
            MidDto65.sample(),
            List.of(MidDto65.sample(), MidDto65.sample()),
            LeafDto65.sample());
    }
}
