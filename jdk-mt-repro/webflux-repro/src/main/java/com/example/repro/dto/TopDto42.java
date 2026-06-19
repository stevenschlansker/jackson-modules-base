package com.example.repro.dto;

import java.util.List;

public record TopDto42(
        long id42,
        String name42,
        boolean flag42,
        int score42,
        MidDto42 detail42,
        List<MidDto42> children42,
        LeafDto42 extra42
) {
    public static TopDto42 sample() {
        return new TopDto42(
            314802L, "top-42", true, 992,
            MidDto42.sample(),
            List.of(MidDto42.sample(), MidDto42.sample()),
            LeafDto42.sample());
    }
}
