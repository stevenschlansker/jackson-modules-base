package com.example.repro.dto;

import java.util.List;

public record TopDto44(
        long id44,
        String name44,
        boolean flag44,
        int score44,
        MidDto44 detail44,
        List<MidDto44> children44,
        LeafDto44 extra44
) {
    public static TopDto44 sample() {
        return new TopDto44(
            663347L, "top-44", true, 978,
            MidDto44.sample(),
            List.of(MidDto44.sample(), MidDto44.sample()),
            LeafDto44.sample());
    }
}
