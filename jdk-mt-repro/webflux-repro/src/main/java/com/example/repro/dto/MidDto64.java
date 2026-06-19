package com.example.repro.dto;

import java.util.List;

public record MidDto64(
        LeafDto64 primary64,
        List<LeafDto64> others64,
        int rank64,
        boolean enabled64,
        String note64
) {
    public static MidDto64 sample() {
        return new MidDto64(
            LeafDto64.sample(),
            List.of(LeafDto64.sample(), LeafDto64.sample()),
            16, true, "mid-64");
    }
}
