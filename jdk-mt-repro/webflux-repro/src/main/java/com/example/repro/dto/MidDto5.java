package com.example.repro.dto;

import java.util.List;

public record MidDto5(
        LeafDto5 primary5,
        List<LeafDto5> others5,
        int rank5,
        boolean enabled5,
        String note5
) {
    public static MidDto5 sample() {
        return new MidDto5(
            LeafDto5.sample(),
            List.of(LeafDto5.sample(), LeafDto5.sample()),
            43, false, "mid-5");
    }
}
