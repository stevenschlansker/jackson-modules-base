package com.example.repro.dto;

import java.util.List;

public record TopDto67(
        long id67,
        String name67,
        boolean flag67,
        int score67,
        MidDto67 detail67,
        List<MidDto67> children67,
        LeafDto67 extra67
) {
    public static TopDto67 sample() {
        return new TopDto67(
            538682L, "top-67", true, 829,
            MidDto67.sample(),
            List.of(MidDto67.sample(), MidDto67.sample()),
            LeafDto67.sample());
    }
}
