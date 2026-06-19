package com.example.repro.dto;

import java.util.List;

public record MidDto65(
        LeafDto65 primary65,
        List<LeafDto65> others65,
        int rank65,
        boolean enabled65,
        String note65
) {
    public static MidDto65 sample() {
        return new MidDto65(
            LeafDto65.sample(),
            List.of(LeafDto65.sample(), LeafDto65.sample()),
            33, false, "mid-65");
    }
}
