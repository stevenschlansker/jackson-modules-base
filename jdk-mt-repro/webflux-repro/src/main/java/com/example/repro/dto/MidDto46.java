package com.example.repro.dto;

import java.util.List;

public record MidDto46(
        LeafDto46 primary46,
        List<LeafDto46> others46,
        int rank46,
        boolean enabled46,
        String note46
) {
    public static MidDto46 sample() {
        return new MidDto46(
            LeafDto46.sample(),
            List.of(LeafDto46.sample(), LeafDto46.sample()),
            13, false, "mid-46");
    }
}
