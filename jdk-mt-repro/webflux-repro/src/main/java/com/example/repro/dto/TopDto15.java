package com.example.repro.dto;

import java.util.List;

public record TopDto15(
        long id15,
        String name15,
        boolean flag15,
        int score15,
        MidDto15 detail15,
        List<MidDto15> children15,
        LeafDto15 extra15
) {
    public static TopDto15 sample() {
        return new TopDto15(
            35027L, "top-15", false, 923,
            MidDto15.sample(),
            List.of(MidDto15.sample(), MidDto15.sample()),
            LeafDto15.sample());
    }
}
