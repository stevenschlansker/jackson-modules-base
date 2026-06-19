package com.example.repro.dto;

import java.util.List;

public record MidDto31(
        LeafDto31 primary31,
        List<LeafDto31> others31,
        int rank31,
        boolean enabled31,
        String note31
) {
    public static MidDto31 sample() {
        return new MidDto31(
            LeafDto31.sample(),
            List.of(LeafDto31.sample(), LeafDto31.sample()),
            37, true, "mid-31");
    }
}
