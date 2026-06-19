package com.example.repro.dto;

import java.util.List;

public record TopDto29(
        long id29,
        String name29,
        boolean flag29,
        int score29,
        MidDto29 detail29,
        List<MidDto29> children29,
        LeafDto29 extra29
) {
    public static TopDto29 sample() {
        return new TopDto29(
            452107L, "top-29", true, 206,
            MidDto29.sample(),
            List.of(MidDto29.sample(), MidDto29.sample()),
            LeafDto29.sample());
    }
}
