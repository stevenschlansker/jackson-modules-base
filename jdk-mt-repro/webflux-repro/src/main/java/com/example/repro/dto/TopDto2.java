package com.example.repro.dto;

import java.util.List;

public record TopDto2(
        long id2,
        String name2,
        boolean flag2,
        int score2,
        MidDto2 detail2,
        List<MidDto2> children2,
        LeafDto2 extra2
) {
    public static TopDto2 sample() {
        return new TopDto2(
            411243L, "top-2", true, 30,
            MidDto2.sample(),
            List.of(MidDto2.sample(), MidDto2.sample()),
            LeafDto2.sample());
    }
}
