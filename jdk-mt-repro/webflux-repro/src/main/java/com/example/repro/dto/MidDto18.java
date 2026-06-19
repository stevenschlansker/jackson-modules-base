package com.example.repro.dto;

import java.util.List;

public record MidDto18(
        LeafDto18 primary18,
        List<LeafDto18> others18,
        int rank18,
        boolean enabled18,
        String note18
) {
    public static MidDto18 sample() {
        return new MidDto18(
            LeafDto18.sample(),
            List.of(LeafDto18.sample(), LeafDto18.sample()),
            30, false, "mid-18");
    }
}
