package com.example.repro.dto;

import java.util.List;

public record MidDto38(
        LeafDto38 primary38,
        List<LeafDto38> others38,
        int rank38,
        boolean enabled38,
        String note38
) {
    public static MidDto38 sample() {
        return new MidDto38(
            LeafDto38.sample(),
            List.of(LeafDto38.sample(), LeafDto38.sample()),
            42, true, "mid-38");
    }
}
