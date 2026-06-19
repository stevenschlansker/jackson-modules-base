package com.example.repro.dto;

import java.util.List;

public record MidDto94(
        LeafDto94 primary94,
        List<LeafDto94> others94,
        int rank94,
        boolean enabled94,
        String note94
) {
    public static MidDto94 sample() {
        return new MidDto94(
            LeafDto94.sample(),
            List.of(LeafDto94.sample(), LeafDto94.sample()),
            8, false, "mid-94");
    }
}
