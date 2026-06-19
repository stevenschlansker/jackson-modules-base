package com.example.repro.dto;

import java.util.List;

public record MidDto88(
        LeafDto88 primary88,
        List<LeafDto88> others88,
        int rank88,
        boolean enabled88,
        String note88
) {
    public static MidDto88 sample() {
        return new MidDto88(
            LeafDto88.sample(),
            List.of(LeafDto88.sample(), LeafDto88.sample()),
            17, true, "mid-88");
    }
}
