package com.example.repro.dto;

import java.util.List;

public record MidDto45(
        LeafDto45 primary45,
        List<LeafDto45> others45,
        int rank45,
        boolean enabled45,
        String note45
) {
    public static MidDto45 sample() {
        return new MidDto45(
            LeafDto45.sample(),
            List.of(LeafDto45.sample(), LeafDto45.sample()),
            42, false, "mid-45");
    }
}
