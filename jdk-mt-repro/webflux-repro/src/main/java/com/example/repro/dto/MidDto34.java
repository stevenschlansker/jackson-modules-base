package com.example.repro.dto;

import java.util.List;

public record MidDto34(
        LeafDto34 primary34,
        List<LeafDto34> others34,
        int rank34,
        boolean enabled34,
        String note34
) {
    public static MidDto34 sample() {
        return new MidDto34(
            LeafDto34.sample(),
            List.of(LeafDto34.sample(), LeafDto34.sample()),
            15, true, "mid-34");
    }
}
