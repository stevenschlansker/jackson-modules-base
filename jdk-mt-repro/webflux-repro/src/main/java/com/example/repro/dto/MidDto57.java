package com.example.repro.dto;

import java.util.List;

public record MidDto57(
        LeafDto57 primary57,
        List<LeafDto57> others57,
        int rank57,
        boolean enabled57,
        String note57
) {
    public static MidDto57 sample() {
        return new MidDto57(
            LeafDto57.sample(),
            List.of(LeafDto57.sample(), LeafDto57.sample()),
            21, false, "mid-57");
    }
}
