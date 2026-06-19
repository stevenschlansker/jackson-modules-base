package com.example.repro.dto;

import java.util.List;

public record MidDto77(
        LeafDto77 primary77,
        List<LeafDto77> others77,
        int rank77,
        boolean enabled77,
        String note77
) {
    public static MidDto77 sample() {
        return new MidDto77(
            LeafDto77.sample(),
            List.of(LeafDto77.sample(), LeafDto77.sample()),
            18, false, "mid-77");
    }
}
