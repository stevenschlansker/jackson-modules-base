package com.example.repro.dto;

import java.util.List;

public record MidDto13(
        LeafDto13 primary13,
        List<LeafDto13> others13,
        int rank13,
        boolean enabled13,
        String note13
) {
    public static MidDto13 sample() {
        return new MidDto13(
            LeafDto13.sample(),
            List.of(LeafDto13.sample(), LeafDto13.sample()),
            28, true, "mid-13");
    }
}
