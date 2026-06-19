package com.example.repro.dto;

import java.util.List;

public record MidDto6(
        LeafDto6 primary6,
        List<LeafDto6> others6,
        int rank6,
        boolean enabled6,
        String note6
) {
    public static MidDto6 sample() {
        return new MidDto6(
            LeafDto6.sample(),
            List.of(LeafDto6.sample(), LeafDto6.sample()),
            11, true, "mid-6");
    }
}
