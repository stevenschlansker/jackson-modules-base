package com.example.repro.dto;

import java.util.List;

public record TopDto71(
        long id71,
        String name71,
        boolean flag71,
        int score71,
        MidDto71 detail71,
        List<MidDto71> children71,
        LeafDto71 extra71
) {
    public static TopDto71 sample() {
        return new TopDto71(
            60927L, "top-71", true, 959,
            MidDto71.sample(),
            List.of(MidDto71.sample(), MidDto71.sample()),
            LeafDto71.sample());
    }
}
