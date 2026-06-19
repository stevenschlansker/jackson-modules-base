package com.example.repro.dto;

import java.util.List;

public record MidDto98(
        LeafDto98 primary98,
        List<LeafDto98> others98,
        int rank98,
        boolean enabled98,
        String note98
) {
    public static MidDto98 sample() {
        return new MidDto98(
            LeafDto98.sample(),
            List.of(LeafDto98.sample(), LeafDto98.sample()),
            6, false, "mid-98");
    }
}
