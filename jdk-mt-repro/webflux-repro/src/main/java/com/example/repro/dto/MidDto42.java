package com.example.repro.dto;

import java.util.List;

public record MidDto42(
        LeafDto42 primary42,
        List<LeafDto42> others42,
        int rank42,
        boolean enabled42,
        String note42
) {
    public static MidDto42 sample() {
        return new MidDto42(
            LeafDto42.sample(),
            List.of(LeafDto42.sample(), LeafDto42.sample()),
            2, false, "mid-42");
    }
}
