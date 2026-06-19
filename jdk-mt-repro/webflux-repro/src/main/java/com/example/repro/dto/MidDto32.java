package com.example.repro.dto;

import java.util.List;

public record MidDto32(
        LeafDto32 primary32,
        List<LeafDto32> others32,
        int rank32,
        boolean enabled32,
        String note32
) {
    public static MidDto32 sample() {
        return new MidDto32(
            LeafDto32.sample(),
            List.of(LeafDto32.sample(), LeafDto32.sample()),
            19, false, "mid-32");
    }
}
