package com.example.repro.dto;

import java.util.List;

public record TopDto64(
        long id64,
        String name64,
        boolean flag64,
        int score64,
        MidDto64 detail64,
        List<MidDto64> children64,
        LeafDto64 extra64
) {
    public static TopDto64 sample() {
        return new TopDto64(
            315350L, "top-64", true, 794,
            MidDto64.sample(),
            List.of(MidDto64.sample(), MidDto64.sample()),
            LeafDto64.sample());
    }
}
