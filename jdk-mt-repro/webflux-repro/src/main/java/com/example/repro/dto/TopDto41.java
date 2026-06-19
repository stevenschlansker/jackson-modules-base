package com.example.repro.dto;

import java.util.List;

public record TopDto41(
        long id41,
        String name41,
        boolean flag41,
        int score41,
        MidDto41 detail41,
        List<MidDto41> children41,
        LeafDto41 extra41
) {
    public static TopDto41 sample() {
        return new TopDto41(
            261366L, "top-41", true, 894,
            MidDto41.sample(),
            List.of(MidDto41.sample(), MidDto41.sample()),
            LeafDto41.sample());
    }
}
