package com.example.repro.dto;

import java.util.List;

public record MidDto30(
        LeafDto30 primary30,
        List<LeafDto30> others30,
        int rank30,
        boolean enabled30,
        String note30
) {
    public static MidDto30 sample() {
        return new MidDto30(
            LeafDto30.sample(),
            List.of(LeafDto30.sample(), LeafDto30.sample()),
            10, false, "mid-30");
    }
}
