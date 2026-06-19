package com.example.repro.dto;

import java.util.List;

public record MidDto97(
        LeafDto97 primary97,
        List<LeafDto97> others97,
        int rank97,
        boolean enabled97,
        String note97
) {
    public static MidDto97 sample() {
        return new MidDto97(
            LeafDto97.sample(),
            List.of(LeafDto97.sample(), LeafDto97.sample()),
            37, true, "mid-97");
    }
}
