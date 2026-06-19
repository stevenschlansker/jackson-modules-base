package com.example.repro.dto;

import java.util.List;

public record MidDto63(
        LeafDto63 primary63,
        List<LeafDto63> others63,
        int rank63,
        boolean enabled63,
        String note63
) {
    public static MidDto63 sample() {
        return new MidDto63(
            LeafDto63.sample(),
            List.of(LeafDto63.sample(), LeafDto63.sample()),
            42, true, "mid-63");
    }
}
