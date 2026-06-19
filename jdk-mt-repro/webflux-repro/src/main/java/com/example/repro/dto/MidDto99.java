package com.example.repro.dto;

import java.util.List;

public record MidDto99(
        LeafDto99 primary99,
        List<LeafDto99> others99,
        int rank99,
        boolean enabled99,
        String note99
) {
    public static MidDto99 sample() {
        return new MidDto99(
            LeafDto99.sample(),
            List.of(LeafDto99.sample(), LeafDto99.sample()),
            32, false, "mid-99");
    }
}
