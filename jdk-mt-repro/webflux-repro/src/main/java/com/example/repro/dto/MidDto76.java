package com.example.repro.dto;

import java.util.List;

public record MidDto76(
        LeafDto76 primary76,
        List<LeafDto76> others76,
        int rank76,
        boolean enabled76,
        String note76
) {
    public static MidDto76 sample() {
        return new MidDto76(
            LeafDto76.sample(),
            List.of(LeafDto76.sample(), LeafDto76.sample()),
            11, false, "mid-76");
    }
}
