package com.example.repro.dto;

import java.util.List;

public record MidDto82(
        LeafDto82 primary82,
        List<LeafDto82> others82,
        int rank82,
        boolean enabled82,
        String note82
) {
    public static MidDto82 sample() {
        return new MidDto82(
            LeafDto82.sample(),
            List.of(LeafDto82.sample(), LeafDto82.sample()),
            48, true, "mid-82");
    }
}
