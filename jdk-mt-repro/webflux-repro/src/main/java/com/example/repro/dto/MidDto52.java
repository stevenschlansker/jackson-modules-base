package com.example.repro.dto;

import java.util.List;

public record MidDto52(
        LeafDto52 primary52,
        List<LeafDto52> others52,
        int rank52,
        boolean enabled52,
        String note52
) {
    public static MidDto52 sample() {
        return new MidDto52(
            LeafDto52.sample(),
            List.of(LeafDto52.sample(), LeafDto52.sample()),
            7, true, "mid-52");
    }
}
