package com.example.repro.dto;

import java.util.List;

public record TopDto92(
        long id92,
        String name92,
        boolean flag92,
        int score92,
        MidDto92 detail92,
        List<MidDto92> children92,
        LeafDto92 extra92
) {
    public static TopDto92 sample() {
        return new TopDto92(
            897859L, "top-92", true, 241,
            MidDto92.sample(),
            List.of(MidDto92.sample(), MidDto92.sample()),
            LeafDto92.sample());
    }
}
