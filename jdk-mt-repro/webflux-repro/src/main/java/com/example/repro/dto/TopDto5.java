package com.example.repro.dto;

import java.util.List;

public record TopDto5(
        long id5,
        String name5,
        boolean flag5,
        int score5,
        MidDto5 detail5,
        List<MidDto5> children5,
        LeafDto5 extra5
) {
    public static TopDto5 sample() {
        return new TopDto5(
            524440L, "top-5", true, 756,
            MidDto5.sample(),
            List.of(MidDto5.sample(), MidDto5.sample()),
            LeafDto5.sample());
    }
}
