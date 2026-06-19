package com.example.repro.dto;

import java.util.List;

public record MidDto0(
        LeafDto0 primary0,
        List<LeafDto0> others0,
        int rank0,
        boolean enabled0,
        String note0
) {
    public static MidDto0 sample() {
        return new MidDto0(
            LeafDto0.sample(),
            List.of(LeafDto0.sample(), LeafDto0.sample()),
            20, true, "mid-0");
    }
}
