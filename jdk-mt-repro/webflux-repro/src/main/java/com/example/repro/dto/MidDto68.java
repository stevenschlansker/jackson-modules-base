package com.example.repro.dto;

import java.util.List;

public record MidDto68(
        LeafDto68 primary68,
        List<LeafDto68> others68,
        int rank68,
        boolean enabled68,
        String note68
) {
    public static MidDto68 sample() {
        return new MidDto68(
            LeafDto68.sample(),
            List.of(LeafDto68.sample(), LeafDto68.sample()),
            23, true, "mid-68");
    }
}
