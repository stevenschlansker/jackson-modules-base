package com.example.repro.dto;

import java.util.List;

public record TopDto6(
        long id6,
        String name6,
        boolean flag6,
        int score6,
        MidDto6 detail6,
        List<MidDto6> children6,
        LeafDto6 extra6
) {
    public static TopDto6 sample() {
        return new TopDto6(
            25897L, "top-6", true, 552,
            MidDto6.sample(),
            List.of(MidDto6.sample(), MidDto6.sample()),
            LeafDto6.sample());
    }
}
