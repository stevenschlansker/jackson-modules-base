package com.example.repro.dto;

import java.util.List;

public record TopDto4(
        long id4,
        String name4,
        boolean flag4,
        int score4,
        MidDto4 detail4,
        List<MidDto4> children4,
        LeafDto4 extra4
) {
    public static TopDto4 sample() {
        return new TopDto4(
            299823L, "top-4", false, 975,
            MidDto4.sample(),
            List.of(MidDto4.sample(), MidDto4.sample()),
            LeafDto4.sample());
    }
}
