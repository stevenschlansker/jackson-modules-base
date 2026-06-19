package com.example.repro.dto;

import java.util.List;

public record TopDto33(
        long id33,
        String name33,
        boolean flag33,
        int score33,
        MidDto33 detail33,
        List<MidDto33> children33,
        LeafDto33 extra33
) {
    public static TopDto33 sample() {
        return new TopDto33(
            666666L, "top-33", true, 569,
            MidDto33.sample(),
            List.of(MidDto33.sample(), MidDto33.sample()),
            LeafDto33.sample());
    }
}
