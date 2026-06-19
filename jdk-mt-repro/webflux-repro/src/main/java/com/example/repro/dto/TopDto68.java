package com.example.repro.dto;

import java.util.List;

public record TopDto68(
        long id68,
        String name68,
        boolean flag68,
        int score68,
        MidDto68 detail68,
        List<MidDto68> children68,
        LeafDto68 extra68
) {
    public static TopDto68 sample() {
        return new TopDto68(
            156973L, "top-68", true, 199,
            MidDto68.sample(),
            List.of(MidDto68.sample(), MidDto68.sample()),
            LeafDto68.sample());
    }
}
