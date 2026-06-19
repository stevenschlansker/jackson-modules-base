package com.example.repro.dto;

import java.util.List;

public record MidDto96(
        LeafDto96 primary96,
        List<LeafDto96> others96,
        int rank96,
        boolean enabled96,
        String note96
) {
    public static MidDto96 sample() {
        return new MidDto96(
            LeafDto96.sample(),
            List.of(LeafDto96.sample(), LeafDto96.sample()),
            17, false, "mid-96");
    }
}
