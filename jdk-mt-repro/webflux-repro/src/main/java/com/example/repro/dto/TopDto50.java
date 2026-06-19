package com.example.repro.dto;

import java.util.List;

public record TopDto50(
        long id50,
        String name50,
        boolean flag50,
        int score50,
        MidDto50 detail50,
        List<MidDto50> children50,
        LeafDto50 extra50
) {
    public static TopDto50 sample() {
        return new TopDto50(
            12864L, "top-50", false, 638,
            MidDto50.sample(),
            List.of(MidDto50.sample(), MidDto50.sample()),
            LeafDto50.sample());
    }
}
