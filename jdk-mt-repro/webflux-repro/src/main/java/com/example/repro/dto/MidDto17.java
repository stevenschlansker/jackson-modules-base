package com.example.repro.dto;

import java.util.List;

public record MidDto17(
        LeafDto17 primary17,
        List<LeafDto17> others17,
        int rank17,
        boolean enabled17,
        String note17
) {
    public static MidDto17 sample() {
        return new MidDto17(
            LeafDto17.sample(),
            List.of(LeafDto17.sample(), LeafDto17.sample()),
            1, true, "mid-17");
    }
}
