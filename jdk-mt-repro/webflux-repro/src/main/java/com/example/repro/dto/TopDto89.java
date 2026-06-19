package com.example.repro.dto;

import java.util.List;

public record TopDto89(
        long id89,
        String name89,
        boolean flag89,
        int score89,
        MidDto89 detail89,
        List<MidDto89> children89,
        LeafDto89 extra89
) {
    public static TopDto89 sample() {
        return new TopDto89(
            832052L, "top-89", true, 803,
            MidDto89.sample(),
            List.of(MidDto89.sample(), MidDto89.sample()),
            LeafDto89.sample());
    }
}
