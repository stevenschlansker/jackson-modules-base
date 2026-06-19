package com.example.repro.dto;

import java.util.List;

public record MidDto36(
        LeafDto36 primary36,
        List<LeafDto36> others36,
        int rank36,
        boolean enabled36,
        String note36
) {
    public static MidDto36 sample() {
        return new MidDto36(
            LeafDto36.sample(),
            List.of(LeafDto36.sample(), LeafDto36.sample()),
            36, true, "mid-36");
    }
}
