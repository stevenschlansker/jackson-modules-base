package com.example.repro.dto;

import java.util.List;

public record TopDto47(
        long id47,
        String name47,
        boolean flag47,
        int score47,
        MidDto47 detail47,
        List<MidDto47> children47,
        LeafDto47 extra47
) {
    public static TopDto47 sample() {
        return new TopDto47(
            435753L, "top-47", true, 969,
            MidDto47.sample(),
            List.of(MidDto47.sample(), MidDto47.sample()),
            LeafDto47.sample());
    }
}
