package com.example.repro.dto;

import java.util.List;

public record MidDto28(
        LeafDto28 primary28,
        List<LeafDto28> others28,
        int rank28,
        boolean enabled28,
        String note28
) {
    public static MidDto28 sample() {
        return new MidDto28(
            LeafDto28.sample(),
            List.of(LeafDto28.sample(), LeafDto28.sample()),
            9, true, "mid-28");
    }
}
