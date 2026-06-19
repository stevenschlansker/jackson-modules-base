package com.example.repro.dto;

import java.util.List;

public record MidDto48(
        LeafDto48 primary48,
        List<LeafDto48> others48,
        int rank48,
        boolean enabled48,
        String note48
) {
    public static MidDto48 sample() {
        return new MidDto48(
            LeafDto48.sample(),
            List.of(LeafDto48.sample(), LeafDto48.sample()),
            29, false, "mid-48");
    }
}
