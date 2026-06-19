package com.example.repro.dto;

import java.util.List;

public record MidDto79(
        LeafDto79 primary79,
        List<LeafDto79> others79,
        int rank79,
        boolean enabled79,
        String note79
) {
    public static MidDto79 sample() {
        return new MidDto79(
            LeafDto79.sample(),
            List.of(LeafDto79.sample(), LeafDto79.sample()),
            4, false, "mid-79");
    }
}
