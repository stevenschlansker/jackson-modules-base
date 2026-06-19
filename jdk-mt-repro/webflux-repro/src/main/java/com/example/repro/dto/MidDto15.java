package com.example.repro.dto;

import java.util.List;

public record MidDto15(
        LeafDto15 primary15,
        List<LeafDto15> others15,
        int rank15,
        boolean enabled15,
        String note15
) {
    public static MidDto15 sample() {
        return new MidDto15(
            LeafDto15.sample(),
            List.of(LeafDto15.sample(), LeafDto15.sample()),
            33, false, "mid-15");
    }
}
