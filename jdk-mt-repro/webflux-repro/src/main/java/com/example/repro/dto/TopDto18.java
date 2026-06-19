package com.example.repro.dto;

import java.util.List;

public record TopDto18(
        long id18,
        String name18,
        boolean flag18,
        int score18,
        MidDto18 detail18,
        List<MidDto18> children18,
        LeafDto18 extra18
) {
    public static TopDto18 sample() {
        return new TopDto18(
            559029L, "top-18", false, 324,
            MidDto18.sample(),
            List.of(MidDto18.sample(), MidDto18.sample()),
            LeafDto18.sample());
    }
}
