package com.example.repro.dto;

import java.util.List;

public record MidDto89(
        LeafDto89 primary89,
        List<LeafDto89> others89,
        int rank89,
        boolean enabled89,
        String note89
) {
    public static MidDto89 sample() {
        return new MidDto89(
            LeafDto89.sample(),
            List.of(LeafDto89.sample(), LeafDto89.sample()),
            20, true, "mid-89");
    }
}
