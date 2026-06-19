package com.example.repro.dto;

import java.util.List;

public record TopDto7(
        long id7,
        String name7,
        boolean flag7,
        int score7,
        MidDto7 detail7,
        List<MidDto7> children7,
        LeafDto7 extra7
) {
    public static TopDto7 sample() {
        return new TopDto7(
            669499L, "top-7", false, 501,
            MidDto7.sample(),
            List.of(MidDto7.sample(), MidDto7.sample()),
            LeafDto7.sample());
    }
}
