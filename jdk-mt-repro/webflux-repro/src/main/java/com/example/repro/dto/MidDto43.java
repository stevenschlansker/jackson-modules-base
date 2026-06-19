package com.example.repro.dto;

import java.util.List;

public record MidDto43(
        LeafDto43 primary43,
        List<LeafDto43> others43,
        int rank43,
        boolean enabled43,
        String note43
) {
    public static MidDto43 sample() {
        return new MidDto43(
            LeafDto43.sample(),
            List.of(LeafDto43.sample(), LeafDto43.sample()),
            0, true, "mid-43");
    }
}
