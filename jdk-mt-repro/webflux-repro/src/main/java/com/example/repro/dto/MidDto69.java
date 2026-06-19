package com.example.repro.dto;

import java.util.List;

public record MidDto69(
        LeafDto69 primary69,
        List<LeafDto69> others69,
        int rank69,
        boolean enabled69,
        String note69
) {
    public static MidDto69 sample() {
        return new MidDto69(
            LeafDto69.sample(),
            List.of(LeafDto69.sample(), LeafDto69.sample()),
            7, true, "mid-69");
    }
}
