package com.example.repro.dto;

import java.util.List;

public record TopDto97(
        long id97,
        String name97,
        boolean flag97,
        int score97,
        MidDto97 detail97,
        List<MidDto97> children97,
        LeafDto97 extra97
) {
    public static TopDto97 sample() {
        return new TopDto97(
            506364L, "top-97", false, 99,
            MidDto97.sample(),
            List.of(MidDto97.sample(), MidDto97.sample()),
            LeafDto97.sample());
    }
}
