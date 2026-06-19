package com.example.repro.dto;

import java.util.List;

public record TopDto28(
        long id28,
        String name28,
        boolean flag28,
        int score28,
        MidDto28 detail28,
        List<MidDto28> children28,
        LeafDto28 extra28
) {
    public static TopDto28 sample() {
        return new TopDto28(
            874815L, "top-28", true, 358,
            MidDto28.sample(),
            List.of(MidDto28.sample(), MidDto28.sample()),
            LeafDto28.sample());
    }
}
