package com.example.repro.dto;

import java.util.List;

public record TopDto54(
        long id54,
        String name54,
        boolean flag54,
        int score54,
        MidDto54 detail54,
        List<MidDto54> children54,
        LeafDto54 extra54
) {
    public static TopDto54 sample() {
        return new TopDto54(
            11234L, "top-54", false, 713,
            MidDto54.sample(),
            List.of(MidDto54.sample(), MidDto54.sample()),
            LeafDto54.sample());
    }
}
