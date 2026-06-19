package com.example.repro.dto;

import java.util.List;

public record MidDto27(
        LeafDto27 primary27,
        List<LeafDto27> others27,
        int rank27,
        boolean enabled27,
        String note27
) {
    public static MidDto27 sample() {
        return new MidDto27(
            LeafDto27.sample(),
            List.of(LeafDto27.sample(), LeafDto27.sample()),
            41, true, "mid-27");
    }
}
