package com.example.repro.dto;

import java.util.List;

public record MidDto26(
        LeafDto26 primary26,
        List<LeafDto26> others26,
        int rank26,
        boolean enabled26,
        String note26
) {
    public static MidDto26 sample() {
        return new MidDto26(
            LeafDto26.sample(),
            List.of(LeafDto26.sample(), LeafDto26.sample()),
            3, true, "mid-26");
    }
}
