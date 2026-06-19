package com.example.repro.dto;

import java.util.List;

public record TopDto39(
        long id39,
        String name39,
        boolean flag39,
        int score39,
        MidDto39 detail39,
        List<MidDto39> children39,
        LeafDto39 extra39
) {
    public static TopDto39 sample() {
        return new TopDto39(
            897212L, "top-39", false, 732,
            MidDto39.sample(),
            List.of(MidDto39.sample(), MidDto39.sample()),
            LeafDto39.sample());
    }
}
