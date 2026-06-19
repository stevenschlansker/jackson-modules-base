package com.example.repro.dto;

import java.util.List;

public record MidDto4(
        LeafDto4 primary4,
        List<LeafDto4> others4,
        int rank4,
        boolean enabled4,
        String note4
) {
    public static MidDto4 sample() {
        return new MidDto4(
            LeafDto4.sample(),
            List.of(LeafDto4.sample(), LeafDto4.sample()),
            29, true, "mid-4");
    }
}
