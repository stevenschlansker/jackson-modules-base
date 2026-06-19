package com.example.repro.dto;

import java.util.List;

public record TopDto84(
        long id84,
        String name84,
        boolean flag84,
        int score84,
        MidDto84 detail84,
        List<MidDto84> children84,
        LeafDto84 extra84
) {
    public static TopDto84 sample() {
        return new TopDto84(
            656212L, "top-84", true, 953,
            MidDto84.sample(),
            List.of(MidDto84.sample(), MidDto84.sample()),
            LeafDto84.sample());
    }
}
