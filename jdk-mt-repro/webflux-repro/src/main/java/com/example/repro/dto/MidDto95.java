package com.example.repro.dto;

import java.util.List;

public record MidDto95(
        LeafDto95 primary95,
        List<LeafDto95> others95,
        int rank95,
        boolean enabled95,
        String note95
) {
    public static MidDto95 sample() {
        return new MidDto95(
            LeafDto95.sample(),
            List.of(LeafDto95.sample(), LeafDto95.sample()),
            13, true, "mid-95");
    }
}
