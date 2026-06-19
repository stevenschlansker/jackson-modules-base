package com.example.repro.dto;

import java.util.List;

public record TopDto66(
        long id66,
        String name66,
        boolean flag66,
        int score66,
        MidDto66 detail66,
        List<MidDto66> children66,
        LeafDto66 extra66
) {
    public static TopDto66 sample() {
        return new TopDto66(
            945946L, "top-66", true, 43,
            MidDto66.sample(),
            List.of(MidDto66.sample(), MidDto66.sample()),
            LeafDto66.sample());
    }
}
