package com.example.repro.dto;

import java.util.List;

public record TopDto81(
        long id81,
        String name81,
        boolean flag81,
        int score81,
        MidDto81 detail81,
        List<MidDto81> children81,
        LeafDto81 extra81
) {
    public static TopDto81 sample() {
        return new TopDto81(
            925988L, "top-81", false, 613,
            MidDto81.sample(),
            List.of(MidDto81.sample(), MidDto81.sample()),
            LeafDto81.sample());
    }
}
