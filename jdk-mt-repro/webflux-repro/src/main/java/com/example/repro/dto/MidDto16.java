package com.example.repro.dto;

import java.util.List;

public record MidDto16(
        LeafDto16 primary16,
        List<LeafDto16> others16,
        int rank16,
        boolean enabled16,
        String note16
) {
    public static MidDto16 sample() {
        return new MidDto16(
            LeafDto16.sample(),
            List.of(LeafDto16.sample(), LeafDto16.sample()),
            47, false, "mid-16");
    }
}
