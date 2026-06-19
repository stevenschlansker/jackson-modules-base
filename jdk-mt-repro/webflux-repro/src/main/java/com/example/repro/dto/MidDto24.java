package com.example.repro.dto;

import java.util.List;

public record MidDto24(
        LeafDto24 primary24,
        List<LeafDto24> others24,
        int rank24,
        boolean enabled24,
        String note24
) {
    public static MidDto24 sample() {
        return new MidDto24(
            LeafDto24.sample(),
            List.of(LeafDto24.sample(), LeafDto24.sample()),
            25, false, "mid-24");
    }
}
