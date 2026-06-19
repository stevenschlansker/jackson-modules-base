package com.example.repro.dto;

import java.util.List;

public record TopDto17(
        long id17,
        String name17,
        boolean flag17,
        int score17,
        MidDto17 detail17,
        List<MidDto17> children17,
        LeafDto17 extra17
) {
    public static TopDto17 sample() {
        return new TopDto17(
            629441L, "top-17", true, 996,
            MidDto17.sample(),
            List.of(MidDto17.sample(), MidDto17.sample()),
            LeafDto17.sample());
    }
}
