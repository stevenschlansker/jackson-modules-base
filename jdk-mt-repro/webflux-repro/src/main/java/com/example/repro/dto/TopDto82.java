package com.example.repro.dto;

import java.util.List;

public record TopDto82(
        long id82,
        String name82,
        boolean flag82,
        int score82,
        MidDto82 detail82,
        List<MidDto82> children82,
        LeafDto82 extra82
) {
    public static TopDto82 sample() {
        return new TopDto82(
            883699L, "top-82", false, 388,
            MidDto82.sample(),
            List.of(MidDto82.sample(), MidDto82.sample()),
            LeafDto82.sample());
    }
}
