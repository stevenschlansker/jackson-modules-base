package com.example.repro.dto;

import java.util.List;

public record MidDto62(
        LeafDto62 primary62,
        List<LeafDto62> others62,
        int rank62,
        boolean enabled62,
        String note62
) {
    public static MidDto62 sample() {
        return new MidDto62(
            LeafDto62.sample(),
            List.of(LeafDto62.sample(), LeafDto62.sample()),
            11, false, "mid-62");
    }
}
