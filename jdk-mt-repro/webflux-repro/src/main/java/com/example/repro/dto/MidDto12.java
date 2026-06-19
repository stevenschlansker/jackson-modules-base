package com.example.repro.dto;

import java.util.List;

public record MidDto12(
        LeafDto12 primary12,
        List<LeafDto12> others12,
        int rank12,
        boolean enabled12,
        String note12
) {
    public static MidDto12 sample() {
        return new MidDto12(
            LeafDto12.sample(),
            List.of(LeafDto12.sample(), LeafDto12.sample()),
            47, false, "mid-12");
    }
}
