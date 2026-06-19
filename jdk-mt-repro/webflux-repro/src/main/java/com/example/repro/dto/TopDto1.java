package com.example.repro.dto;

import java.util.List;

public record TopDto1(
        long id1,
        String name1,
        boolean flag1,
        int score1,
        MidDto1 detail1,
        List<MidDto1> children1,
        LeafDto1 extra1
) {
    public static TopDto1 sample() {
        return new TopDto1(
            678032L, "top-1", false, 170,
            MidDto1.sample(),
            List.of(MidDto1.sample(), MidDto1.sample()),
            LeafDto1.sample());
    }
}
