package com.example.repro.dto;

import java.util.List;

public record MidDto8(
        LeafDto8 primary8,
        List<LeafDto8> others8,
        int rank8,
        boolean enabled8,
        String note8
) {
    public static MidDto8 sample() {
        return new MidDto8(
            LeafDto8.sample(),
            List.of(LeafDto8.sample(), LeafDto8.sample()),
            36, true, "mid-8");
    }
}
