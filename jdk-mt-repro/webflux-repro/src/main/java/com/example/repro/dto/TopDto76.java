package com.example.repro.dto;

import java.util.List;

public record TopDto76(
        long id76,
        String name76,
        boolean flag76,
        int score76,
        MidDto76 detail76,
        List<MidDto76> children76,
        LeafDto76 extra76
) {
    public static TopDto76 sample() {
        return new TopDto76(
            709828L, "top-76", false, 987,
            MidDto76.sample(),
            List.of(MidDto76.sample(), MidDto76.sample()),
            LeafDto76.sample());
    }
}
