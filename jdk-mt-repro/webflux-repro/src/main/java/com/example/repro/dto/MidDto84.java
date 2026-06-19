package com.example.repro.dto;

import java.util.List;

public record MidDto84(
        LeafDto84 primary84,
        List<LeafDto84> others84,
        int rank84,
        boolean enabled84,
        String note84
) {
    public static MidDto84 sample() {
        return new MidDto84(
            LeafDto84.sample(),
            List.of(LeafDto84.sample(), LeafDto84.sample()),
            24, false, "mid-84");
    }
}
