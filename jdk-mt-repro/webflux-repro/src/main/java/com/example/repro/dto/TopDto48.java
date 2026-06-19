package com.example.repro.dto;

import java.util.List;

public record TopDto48(
        long id48,
        String name48,
        boolean flag48,
        int score48,
        MidDto48 detail48,
        List<MidDto48> children48,
        LeafDto48 extra48
) {
    public static TopDto48 sample() {
        return new TopDto48(
            85904L, "top-48", false, 54,
            MidDto48.sample(),
            List.of(MidDto48.sample(), MidDto48.sample()),
            LeafDto48.sample());
    }
}
