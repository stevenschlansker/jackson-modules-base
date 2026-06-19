package com.example.repro.dto;

import java.util.List;

public record MidDto73(
        LeafDto73 primary73,
        List<LeafDto73> others73,
        int rank73,
        boolean enabled73,
        String note73
) {
    public static MidDto73 sample() {
        return new MidDto73(
            LeafDto73.sample(),
            List.of(LeafDto73.sample(), LeafDto73.sample()),
            6, true, "mid-73");
    }
}
