package com.example.repro.dto;

import java.util.List;

public record MidDto19(
        LeafDto19 primary19,
        List<LeafDto19> others19,
        int rank19,
        boolean enabled19,
        String note19
) {
    public static MidDto19 sample() {
        return new MidDto19(
            LeafDto19.sample(),
            List.of(LeafDto19.sample(), LeafDto19.sample()),
            31, false, "mid-19");
    }
}
