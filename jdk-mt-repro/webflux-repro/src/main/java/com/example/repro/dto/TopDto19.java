package com.example.repro.dto;

import java.util.List;

public record TopDto19(
        long id19,
        String name19,
        boolean flag19,
        int score19,
        MidDto19 detail19,
        List<MidDto19> children19,
        LeafDto19 extra19
) {
    public static TopDto19 sample() {
        return new TopDto19(
            896996L, "top-19", false, 342,
            MidDto19.sample(),
            List.of(MidDto19.sample(), MidDto19.sample()),
            LeafDto19.sample());
    }
}
