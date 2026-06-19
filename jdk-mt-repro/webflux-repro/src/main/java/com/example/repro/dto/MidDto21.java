package com.example.repro.dto;

import java.util.List;

public record MidDto21(
        LeafDto21 primary21,
        List<LeafDto21> others21,
        int rank21,
        boolean enabled21,
        String note21
) {
    public static MidDto21 sample() {
        return new MidDto21(
            LeafDto21.sample(),
            List.of(LeafDto21.sample(), LeafDto21.sample()),
            28, false, "mid-21");
    }
}
