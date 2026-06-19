package com.example.repro.dto;

import java.util.List;

public record MidDto74(
        LeafDto74 primary74,
        List<LeafDto74> others74,
        int rank74,
        boolean enabled74,
        String note74
) {
    public static MidDto74 sample() {
        return new MidDto74(
            LeafDto74.sample(),
            List.of(LeafDto74.sample(), LeafDto74.sample()),
            5, false, "mid-74");
    }
}
