package com.example.repro.dto;

import java.util.List;

public record TopDto51(
        long id51,
        String name51,
        boolean flag51,
        int score51,
        MidDto51 detail51,
        List<MidDto51> children51,
        LeafDto51 extra51
) {
    public static TopDto51 sample() {
        return new TopDto51(
            609497L, "top-51", true, 174,
            MidDto51.sample(),
            List.of(MidDto51.sample(), MidDto51.sample()),
            LeafDto51.sample());
    }
}
