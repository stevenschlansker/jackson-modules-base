package com.example.repro.dto;

import java.util.List;

public record MidDto22(
        LeafDto22 primary22,
        List<LeafDto22> others22,
        int rank22,
        boolean enabled22,
        String note22
) {
    public static MidDto22 sample() {
        return new MidDto22(
            LeafDto22.sample(),
            List.of(LeafDto22.sample(), LeafDto22.sample()),
            28, true, "mid-22");
    }
}
