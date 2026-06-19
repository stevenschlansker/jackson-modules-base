package com.example.repro.dto;

import java.util.List;

public record MidDto92(
        LeafDto92 primary92,
        List<LeafDto92> others92,
        int rank92,
        boolean enabled92,
        String note92
) {
    public static MidDto92 sample() {
        return new MidDto92(
            LeafDto92.sample(),
            List.of(LeafDto92.sample(), LeafDto92.sample()),
            20, false, "mid-92");
    }
}
