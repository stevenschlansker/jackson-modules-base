package com.example.repro.dto;

import java.util.List;

public record TopDto60(
        long id60,
        String name60,
        boolean flag60,
        int score60,
        MidDto60 detail60,
        List<MidDto60> children60,
        LeafDto60 extra60
) {
    public static TopDto60 sample() {
        return new TopDto60(
            569674L, "top-60", true, 748,
            MidDto60.sample(),
            List.of(MidDto60.sample(), MidDto60.sample()),
            LeafDto60.sample());
    }
}
