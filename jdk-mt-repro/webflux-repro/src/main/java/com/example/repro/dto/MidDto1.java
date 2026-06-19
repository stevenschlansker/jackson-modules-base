package com.example.repro.dto;

import java.util.List;

public record MidDto1(
        LeafDto1 primary1,
        List<LeafDto1> others1,
        int rank1,
        boolean enabled1,
        String note1
) {
    public static MidDto1 sample() {
        return new MidDto1(
            LeafDto1.sample(),
            List.of(LeafDto1.sample(), LeafDto1.sample()),
            42, false, "mid-1");
    }
}
