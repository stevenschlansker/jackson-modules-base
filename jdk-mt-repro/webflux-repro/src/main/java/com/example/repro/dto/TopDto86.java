package com.example.repro.dto;

import java.util.List;

public record TopDto86(
        long id86,
        String name86,
        boolean flag86,
        int score86,
        MidDto86 detail86,
        List<MidDto86> children86,
        LeafDto86 extra86
) {
    public static TopDto86 sample() {
        return new TopDto86(
            907685L, "top-86", false, 849,
            MidDto86.sample(),
            List.of(MidDto86.sample(), MidDto86.sample()),
            LeafDto86.sample());
    }
}
