package com.example.repro.dto;

import java.util.List;

public record TopDto94(
        long id94,
        String name94,
        boolean flag94,
        int score94,
        MidDto94 detail94,
        List<MidDto94> children94,
        LeafDto94 extra94
) {
    public static TopDto94 sample() {
        return new TopDto94(
            572552L, "top-94", true, 391,
            MidDto94.sample(),
            List.of(MidDto94.sample(), MidDto94.sample()),
            LeafDto94.sample());
    }
}
