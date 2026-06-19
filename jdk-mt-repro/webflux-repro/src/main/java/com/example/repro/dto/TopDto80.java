package com.example.repro.dto;

import java.util.List;

public record TopDto80(
        long id80,
        String name80,
        boolean flag80,
        int score80,
        MidDto80 detail80,
        List<MidDto80> children80,
        LeafDto80 extra80
) {
    public static TopDto80 sample() {
        return new TopDto80(
            421508L, "top-80", true, 980,
            MidDto80.sample(),
            List.of(MidDto80.sample(), MidDto80.sample()),
            LeafDto80.sample());
    }
}
