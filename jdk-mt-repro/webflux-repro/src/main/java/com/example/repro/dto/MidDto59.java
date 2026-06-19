package com.example.repro.dto;

import java.util.List;

public record MidDto59(
        LeafDto59 primary59,
        List<LeafDto59> others59,
        int rank59,
        boolean enabled59,
        String note59
) {
    public static MidDto59 sample() {
        return new MidDto59(
            LeafDto59.sample(),
            List.of(LeafDto59.sample(), LeafDto59.sample()),
            15, true, "mid-59");
    }
}
