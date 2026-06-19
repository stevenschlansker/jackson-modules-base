package com.example.repro.dto;

import java.util.List;

public record MidDto85(
        LeafDto85 primary85,
        List<LeafDto85> others85,
        int rank85,
        boolean enabled85,
        String note85
) {
    public static MidDto85 sample() {
        return new MidDto85(
            LeafDto85.sample(),
            List.of(LeafDto85.sample(), LeafDto85.sample()),
            30, false, "mid-85");
    }
}
