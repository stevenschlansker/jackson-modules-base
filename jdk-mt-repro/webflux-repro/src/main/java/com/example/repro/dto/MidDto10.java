package com.example.repro.dto;

import java.util.List;

public record MidDto10(
        LeafDto10 primary10,
        List<LeafDto10> others10,
        int rank10,
        boolean enabled10,
        String note10
) {
    public static MidDto10 sample() {
        return new MidDto10(
            LeafDto10.sample(),
            List.of(LeafDto10.sample(), LeafDto10.sample()),
            9, true, "mid-10");
    }
}
