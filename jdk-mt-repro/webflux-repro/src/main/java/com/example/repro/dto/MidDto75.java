package com.example.repro.dto;

import java.util.List;

public record MidDto75(
        LeafDto75 primary75,
        List<LeafDto75> others75,
        int rank75,
        boolean enabled75,
        String note75
) {
    public static MidDto75 sample() {
        return new MidDto75(
            LeafDto75.sample(),
            List.of(LeafDto75.sample(), LeafDto75.sample()),
            21, true, "mid-75");
    }
}
