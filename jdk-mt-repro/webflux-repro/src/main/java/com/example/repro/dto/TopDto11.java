package com.example.repro.dto;

import java.util.List;

public record TopDto11(
        long id11,
        String name11,
        boolean flag11,
        int score11,
        MidDto11 detail11,
        List<MidDto11> children11,
        LeafDto11 extra11
) {
    public static TopDto11 sample() {
        return new TopDto11(
            141911L, "top-11", false, 246,
            MidDto11.sample(),
            List.of(MidDto11.sample(), MidDto11.sample()),
            LeafDto11.sample());
    }
}
