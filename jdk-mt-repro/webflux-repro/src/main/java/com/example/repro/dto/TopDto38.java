package com.example.repro.dto;

import java.util.List;

public record TopDto38(
        long id38,
        String name38,
        boolean flag38,
        int score38,
        MidDto38 detail38,
        List<MidDto38> children38,
        LeafDto38 extra38
) {
    public static TopDto38 sample() {
        return new TopDto38(
            465959L, "top-38", false, 224,
            MidDto38.sample(),
            List.of(MidDto38.sample(), MidDto38.sample()),
            LeafDto38.sample());
    }
}
