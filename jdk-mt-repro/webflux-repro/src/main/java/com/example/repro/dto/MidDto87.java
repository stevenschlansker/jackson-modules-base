package com.example.repro.dto;

import java.util.List;

public record MidDto87(
        LeafDto87 primary87,
        List<LeafDto87> others87,
        int rank87,
        boolean enabled87,
        String note87
) {
    public static MidDto87 sample() {
        return new MidDto87(
            LeafDto87.sample(),
            List.of(LeafDto87.sample(), LeafDto87.sample()),
            49, true, "mid-87");
    }
}
