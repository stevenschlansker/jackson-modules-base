package com.example.repro.dto;

import java.util.List;

public record MidDto44(
        LeafDto44 primary44,
        List<LeafDto44> others44,
        int rank44,
        boolean enabled44,
        String note44
) {
    public static MidDto44 sample() {
        return new MidDto44(
            LeafDto44.sample(),
            List.of(LeafDto44.sample(), LeafDto44.sample()),
            43, false, "mid-44");
    }
}
