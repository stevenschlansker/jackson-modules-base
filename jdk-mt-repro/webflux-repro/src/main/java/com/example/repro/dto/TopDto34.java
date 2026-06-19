package com.example.repro.dto;

import java.util.List;

public record TopDto34(
        long id34,
        String name34,
        boolean flag34,
        int score34,
        MidDto34 detail34,
        List<MidDto34> children34,
        LeafDto34 extra34
) {
    public static TopDto34 sample() {
        return new TopDto34(
            30984L, "top-34", false, 243,
            MidDto34.sample(),
            List.of(MidDto34.sample(), MidDto34.sample()),
            LeafDto34.sample());
    }
}
