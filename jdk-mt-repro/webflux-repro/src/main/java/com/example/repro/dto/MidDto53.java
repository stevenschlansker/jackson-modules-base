package com.example.repro.dto;

import java.util.List;

public record MidDto53(
        LeafDto53 primary53,
        List<LeafDto53> others53,
        int rank53,
        boolean enabled53,
        String note53
) {
    public static MidDto53 sample() {
        return new MidDto53(
            LeafDto53.sample(),
            List.of(LeafDto53.sample(), LeafDto53.sample()),
            48, true, "mid-53");
    }
}
