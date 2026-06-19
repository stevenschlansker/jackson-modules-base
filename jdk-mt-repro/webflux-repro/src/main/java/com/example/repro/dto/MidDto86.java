package com.example.repro.dto;

import java.util.List;

public record MidDto86(
        LeafDto86 primary86,
        List<LeafDto86> others86,
        int rank86,
        boolean enabled86,
        String note86
) {
    public static MidDto86 sample() {
        return new MidDto86(
            LeafDto86.sample(),
            List.of(LeafDto86.sample(), LeafDto86.sample()),
            17, true, "mid-86");
    }
}
