package com.example.repro.dto;

import java.util.List;

public record MidDto90(
        LeafDto90 primary90,
        List<LeafDto90> others90,
        int rank90,
        boolean enabled90,
        String note90
) {
    public static MidDto90 sample() {
        return new MidDto90(
            LeafDto90.sample(),
            List.of(LeafDto90.sample(), LeafDto90.sample()),
            26, true, "mid-90");
    }
}
