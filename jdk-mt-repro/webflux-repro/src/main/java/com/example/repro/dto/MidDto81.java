package com.example.repro.dto;

import java.util.List;

public record MidDto81(
        LeafDto81 primary81,
        List<LeafDto81> others81,
        int rank81,
        boolean enabled81,
        String note81
) {
    public static MidDto81 sample() {
        return new MidDto81(
            LeafDto81.sample(),
            List.of(LeafDto81.sample(), LeafDto81.sample()),
            18, false, "mid-81");
    }
}
