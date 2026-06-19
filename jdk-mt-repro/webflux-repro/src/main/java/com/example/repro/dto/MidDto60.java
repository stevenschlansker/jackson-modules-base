package com.example.repro.dto;

import java.util.List;

public record MidDto60(
        LeafDto60 primary60,
        List<LeafDto60> others60,
        int rank60,
        boolean enabled60,
        String note60
) {
    public static MidDto60 sample() {
        return new MidDto60(
            LeafDto60.sample(),
            List.of(LeafDto60.sample(), LeafDto60.sample()),
            10, true, "mid-60");
    }
}
