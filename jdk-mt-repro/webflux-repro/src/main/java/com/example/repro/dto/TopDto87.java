package com.example.repro.dto;

import java.util.List;

public record TopDto87(
        long id87,
        String name87,
        boolean flag87,
        int score87,
        MidDto87 detail87,
        List<MidDto87> children87,
        LeafDto87 extra87
) {
    public static TopDto87 sample() {
        return new TopDto87(
            74169L, "top-87", true, 66,
            MidDto87.sample(),
            List.of(MidDto87.sample(), MidDto87.sample()),
            LeafDto87.sample());
    }
}
