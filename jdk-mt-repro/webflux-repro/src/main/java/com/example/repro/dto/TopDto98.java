package com.example.repro.dto;

import java.util.List;

public record TopDto98(
        long id98,
        String name98,
        boolean flag98,
        int score98,
        MidDto98 detail98,
        List<MidDto98> children98,
        LeafDto98 extra98
) {
    public static TopDto98 sample() {
        return new TopDto98(
            297606L, "top-98", false, 722,
            MidDto98.sample(),
            List.of(MidDto98.sample(), MidDto98.sample()),
            LeafDto98.sample());
    }
}
