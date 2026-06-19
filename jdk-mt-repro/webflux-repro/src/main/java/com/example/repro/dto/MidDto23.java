package com.example.repro.dto;

import java.util.List;

public record MidDto23(
        LeafDto23 primary23,
        List<LeafDto23> others23,
        int rank23,
        boolean enabled23,
        String note23
) {
    public static MidDto23 sample() {
        return new MidDto23(
            LeafDto23.sample(),
            List.of(LeafDto23.sample(), LeafDto23.sample()),
            43, true, "mid-23");
    }
}
