package com.example.repro.dto;

import java.util.List;

public record TopDto91(
        long id91,
        String name91,
        boolean flag91,
        int score91,
        MidDto91 detail91,
        List<MidDto91> children91,
        LeafDto91 extra91
) {
    public static TopDto91 sample() {
        return new TopDto91(
            260071L, "top-91", false, 121,
            MidDto91.sample(),
            List.of(MidDto91.sample(), MidDto91.sample()),
            LeafDto91.sample());
    }
}
