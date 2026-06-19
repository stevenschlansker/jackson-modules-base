package com.example.repro.dto;

import java.util.List;

public record TopDto0(
        long id0,
        String name0,
        boolean flag0,
        int score0,
        MidDto0 detail0,
        List<MidDto0> children0,
        LeafDto0 extra0
) {
    public static TopDto0 sample() {
        return new TopDto0(
            21505L, "top-0", true, 519,
            MidDto0.sample(),
            List.of(MidDto0.sample(), MidDto0.sample()),
            LeafDto0.sample());
    }
}
