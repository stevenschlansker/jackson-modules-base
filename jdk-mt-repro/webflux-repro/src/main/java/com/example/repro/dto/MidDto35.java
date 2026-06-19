package com.example.repro.dto;

import java.util.List;

public record MidDto35(
        LeafDto35 primary35,
        List<LeafDto35> others35,
        int rank35,
        boolean enabled35,
        String note35
) {
    public static MidDto35 sample() {
        return new MidDto35(
            LeafDto35.sample(),
            List.of(LeafDto35.sample(), LeafDto35.sample()),
            0, true, "mid-35");
    }
}
