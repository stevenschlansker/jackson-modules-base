package com.example.repro.dto;

import java.util.List;

public record MidDto67(
        LeafDto67 primary67,
        List<LeafDto67> others67,
        int rank67,
        boolean enabled67,
        String note67
) {
    public static MidDto67 sample() {
        return new MidDto67(
            LeafDto67.sample(),
            List.of(LeafDto67.sample(), LeafDto67.sample()),
            28, true, "mid-67");
    }
}
