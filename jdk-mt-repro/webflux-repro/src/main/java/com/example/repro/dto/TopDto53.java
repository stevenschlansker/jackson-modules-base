package com.example.repro.dto;

import java.util.List;

public record TopDto53(
        long id53,
        String name53,
        boolean flag53,
        int score53,
        MidDto53 detail53,
        List<MidDto53> children53,
        LeafDto53 extra53
) {
    public static TopDto53 sample() {
        return new TopDto53(
            62148L, "top-53", false, 367,
            MidDto53.sample(),
            List.of(MidDto53.sample(), MidDto53.sample()),
            LeafDto53.sample());
    }
}
