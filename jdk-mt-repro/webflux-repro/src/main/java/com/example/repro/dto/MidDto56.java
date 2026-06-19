package com.example.repro.dto;

import java.util.List;

public record MidDto56(
        LeafDto56 primary56,
        List<LeafDto56> others56,
        int rank56,
        boolean enabled56,
        String note56
) {
    public static MidDto56 sample() {
        return new MidDto56(
            LeafDto56.sample(),
            List.of(LeafDto56.sample(), LeafDto56.sample()),
            4, false, "mid-56");
    }
}
