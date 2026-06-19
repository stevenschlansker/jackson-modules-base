package com.example.repro.dto;

import java.util.List;

public record MidDto71(
        LeafDto71 primary71,
        List<LeafDto71> others71,
        int rank71,
        boolean enabled71,
        String note71
) {
    public static MidDto71 sample() {
        return new MidDto71(
            LeafDto71.sample(),
            List.of(LeafDto71.sample(), LeafDto71.sample()),
            48, true, "mid-71");
    }
}
