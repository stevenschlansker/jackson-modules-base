package com.example.repro.dto;

import java.util.List;

public record MidDto20(
        LeafDto20 primary20,
        List<LeafDto20> others20,
        int rank20,
        boolean enabled20,
        String note20
) {
    public static MidDto20 sample() {
        return new MidDto20(
            LeafDto20.sample(),
            List.of(LeafDto20.sample(), LeafDto20.sample()),
            24, false, "mid-20");
    }
}
