package com.example.repro.dto;

import java.util.List;

public record MidDto78(
        LeafDto78 primary78,
        List<LeafDto78> others78,
        int rank78,
        boolean enabled78,
        String note78
) {
    public static MidDto78 sample() {
        return new MidDto78(
            LeafDto78.sample(),
            List.of(LeafDto78.sample(), LeafDto78.sample()),
            31, false, "mid-78");
    }
}
