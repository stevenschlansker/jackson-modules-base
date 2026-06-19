package com.example.repro.dto;

import java.util.List;

public record MidDto2(
        LeafDto2 primary2,
        List<LeafDto2> others2,
        int rank2,
        boolean enabled2,
        String note2
) {
    public static MidDto2 sample() {
        return new MidDto2(
            LeafDto2.sample(),
            List.of(LeafDto2.sample(), LeafDto2.sample()),
            26, false, "mid-2");
    }
}
