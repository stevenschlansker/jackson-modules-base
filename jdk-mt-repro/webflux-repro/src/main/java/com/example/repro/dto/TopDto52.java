package com.example.repro.dto;

import java.util.List;

public record TopDto52(
        long id52,
        String name52,
        boolean flag52,
        int score52,
        MidDto52 detail52,
        List<MidDto52> children52,
        LeafDto52 extra52
) {
    public static TopDto52 sample() {
        return new TopDto52(
            2806L, "top-52", true, 480,
            MidDto52.sample(),
            List.of(MidDto52.sample(), MidDto52.sample()),
            LeafDto52.sample());
    }
}
