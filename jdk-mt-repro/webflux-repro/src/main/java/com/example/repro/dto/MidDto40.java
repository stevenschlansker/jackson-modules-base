package com.example.repro.dto;

import java.util.List;

public record MidDto40(
        LeafDto40 primary40,
        List<LeafDto40> others40,
        int rank40,
        boolean enabled40,
        String note40
) {
    public static MidDto40 sample() {
        return new MidDto40(
            LeafDto40.sample(),
            List.of(LeafDto40.sample(), LeafDto40.sample()),
            6, true, "mid-40");
    }
}
