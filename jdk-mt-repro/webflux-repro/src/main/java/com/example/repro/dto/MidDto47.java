package com.example.repro.dto;

import java.util.List;

public record MidDto47(
        LeafDto47 primary47,
        List<LeafDto47> others47,
        int rank47,
        boolean enabled47,
        String note47
) {
    public static MidDto47 sample() {
        return new MidDto47(
            LeafDto47.sample(),
            List.of(LeafDto47.sample(), LeafDto47.sample()),
            38, false, "mid-47");
    }
}
