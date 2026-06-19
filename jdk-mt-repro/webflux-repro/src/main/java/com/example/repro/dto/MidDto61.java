package com.example.repro.dto;

import java.util.List;

public record MidDto61(
        LeafDto61 primary61,
        List<LeafDto61> others61,
        int rank61,
        boolean enabled61,
        String note61
) {
    public static MidDto61 sample() {
        return new MidDto61(
            LeafDto61.sample(),
            List.of(LeafDto61.sample(), LeafDto61.sample()),
            36, false, "mid-61");
    }
}
