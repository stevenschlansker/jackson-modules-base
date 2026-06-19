package com.example.repro.dto;

import java.util.List;

public record MidDto37(
        LeafDto37 primary37,
        List<LeafDto37> others37,
        int rank37,
        boolean enabled37,
        String note37
) {
    public static MidDto37 sample() {
        return new MidDto37(
            LeafDto37.sample(),
            List.of(LeafDto37.sample(), LeafDto37.sample()),
            3, true, "mid-37");
    }
}
