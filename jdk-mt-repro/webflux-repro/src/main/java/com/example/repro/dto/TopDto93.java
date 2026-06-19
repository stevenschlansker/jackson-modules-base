package com.example.repro.dto;

import java.util.List;

public record TopDto93(
        long id93,
        String name93,
        boolean flag93,
        int score93,
        MidDto93 detail93,
        List<MidDto93> children93,
        LeafDto93 extra93
) {
    public static TopDto93 sample() {
        return new TopDto93(
            933935L, "top-93", true, 328,
            MidDto93.sample(),
            List.of(MidDto93.sample(), MidDto93.sample()),
            LeafDto93.sample());
    }
}
