package com.example.repro.dto;

import java.util.List;

public record TopDto72(
        long id72,
        String name72,
        boolean flag72,
        int score72,
        MidDto72 detail72,
        List<MidDto72> children72,
        LeafDto72 extra72
) {
    public static TopDto72 sample() {
        return new TopDto72(
            861818L, "top-72", false, 84,
            MidDto72.sample(),
            List.of(MidDto72.sample(), MidDto72.sample()),
            LeafDto72.sample());
    }
}
