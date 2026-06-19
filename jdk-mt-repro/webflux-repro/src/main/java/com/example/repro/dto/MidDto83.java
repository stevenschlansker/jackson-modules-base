package com.example.repro.dto;

import java.util.List;

public record MidDto83(
        LeafDto83 primary83,
        List<LeafDto83> others83,
        int rank83,
        boolean enabled83,
        String note83
) {
    public static MidDto83 sample() {
        return new MidDto83(
            LeafDto83.sample(),
            List.of(LeafDto83.sample(), LeafDto83.sample()),
            4, false, "mid-83");
    }
}
