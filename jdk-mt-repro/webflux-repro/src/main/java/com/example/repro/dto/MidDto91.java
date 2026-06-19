package com.example.repro.dto;

import java.util.List;

public record MidDto91(
        LeafDto91 primary91,
        List<LeafDto91> others91,
        int rank91,
        boolean enabled91,
        String note91
) {
    public static MidDto91 sample() {
        return new MidDto91(
            LeafDto91.sample(),
            List.of(LeafDto91.sample(), LeafDto91.sample()),
            3, true, "mid-91");
    }
}
