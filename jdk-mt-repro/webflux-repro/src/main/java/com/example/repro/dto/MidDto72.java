package com.example.repro.dto;

import java.util.List;

public record MidDto72(
        LeafDto72 primary72,
        List<LeafDto72> others72,
        int rank72,
        boolean enabled72,
        String note72
) {
    public static MidDto72 sample() {
        return new MidDto72(
            LeafDto72.sample(),
            List.of(LeafDto72.sample(), LeafDto72.sample()),
            10, true, "mid-72");
    }
}
