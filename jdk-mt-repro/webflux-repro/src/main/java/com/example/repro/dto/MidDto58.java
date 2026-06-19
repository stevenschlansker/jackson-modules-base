package com.example.repro.dto;

import java.util.List;

public record MidDto58(
        LeafDto58 primary58,
        List<LeafDto58> others58,
        int rank58,
        boolean enabled58,
        String note58
) {
    public static MidDto58 sample() {
        return new MidDto58(
            LeafDto58.sample(),
            List.of(LeafDto58.sample(), LeafDto58.sample()),
            17, true, "mid-58");
    }
}
