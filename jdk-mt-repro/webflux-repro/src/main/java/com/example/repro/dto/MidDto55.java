package com.example.repro.dto;

import java.util.List;

public record MidDto55(
        LeafDto55 primary55,
        List<LeafDto55> others55,
        int rank55,
        boolean enabled55,
        String note55
) {
    public static MidDto55 sample() {
        return new MidDto55(
            LeafDto55.sample(),
            List.of(LeafDto55.sample(), LeafDto55.sample()),
            3, true, "mid-55");
    }
}
