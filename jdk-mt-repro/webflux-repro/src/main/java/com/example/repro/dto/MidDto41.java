package com.example.repro.dto;

import java.util.List;

public record MidDto41(
        LeafDto41 primary41,
        List<LeafDto41> others41,
        int rank41,
        boolean enabled41,
        String note41
) {
    public static MidDto41 sample() {
        return new MidDto41(
            LeafDto41.sample(),
            List.of(LeafDto41.sample(), LeafDto41.sample()),
            31, false, "mid-41");
    }
}
