package com.example.repro.dto;

import java.util.List;

public record MidDto66(
        LeafDto66 primary66,
        List<LeafDto66> others66,
        int rank66,
        boolean enabled66,
        String note66
) {
    public static MidDto66 sample() {
        return new MidDto66(
            LeafDto66.sample(),
            List.of(LeafDto66.sample(), LeafDto66.sample()),
            32, false, "mid-66");
    }
}
