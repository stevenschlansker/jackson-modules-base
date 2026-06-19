package com.example.repro.dto;

import java.util.List;

public record TopDto9(
        long id9,
        String name9,
        boolean flag9,
        int score9,
        MidDto9 detail9,
        List<MidDto9> children9,
        LeafDto9 extra9
) {
    public static TopDto9 sample() {
        return new TopDto9(
            536744L, "top-9", false, 479,
            MidDto9.sample(),
            List.of(MidDto9.sample(), MidDto9.sample()),
            LeafDto9.sample());
    }
}
