package com.example.repro.dto;

import java.util.List;

public record TopDto30(
        long id30,
        String name30,
        boolean flag30,
        int score30,
        MidDto30 detail30,
        List<MidDto30> children30,
        LeafDto30 extra30
) {
    public static TopDto30 sample() {
        return new TopDto30(
            878186L, "top-30", false, 643,
            MidDto30.sample(),
            List.of(MidDto30.sample(), MidDto30.sample()),
            LeafDto30.sample());
    }
}
