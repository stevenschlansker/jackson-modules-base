package com.example.repro.dto;

import java.util.List;

public record TopDto63(
        long id63,
        String name63,
        boolean flag63,
        int score63,
        MidDto63 detail63,
        List<MidDto63> children63,
        LeafDto63 extra63
) {
    public static TopDto63 sample() {
        return new TopDto63(
            5889L, "top-63", false, 649,
            MidDto63.sample(),
            List.of(MidDto63.sample(), MidDto63.sample()),
            LeafDto63.sample());
    }
}
