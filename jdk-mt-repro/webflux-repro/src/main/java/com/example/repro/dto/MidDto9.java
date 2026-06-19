package com.example.repro.dto;

import java.util.List;

public record MidDto9(
        LeafDto9 primary9,
        List<LeafDto9> others9,
        int rank9,
        boolean enabled9,
        String note9
) {
    public static MidDto9 sample() {
        return new MidDto9(
            LeafDto9.sample(),
            List.of(LeafDto9.sample(), LeafDto9.sample()),
            43, true, "mid-9");
    }
}
