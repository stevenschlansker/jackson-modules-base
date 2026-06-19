package com.example.repro.dto;

import java.util.List;

public record MidDto49(
        LeafDto49 primary49,
        List<LeafDto49> others49,
        int rank49,
        boolean enabled49,
        String note49
) {
    public static MidDto49 sample() {
        return new MidDto49(
            LeafDto49.sample(),
            List.of(LeafDto49.sample(), LeafDto49.sample()),
            48, false, "mid-49");
    }
}
