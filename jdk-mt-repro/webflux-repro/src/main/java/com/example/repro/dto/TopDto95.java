package com.example.repro.dto;

import java.util.List;

public record TopDto95(
        long id95,
        String name95,
        boolean flag95,
        int score95,
        MidDto95 detail95,
        List<MidDto95> children95,
        LeafDto95 extra95
) {
    public static TopDto95 sample() {
        return new TopDto95(
            402371L, "top-95", false, 144,
            MidDto95.sample(),
            List.of(MidDto95.sample(), MidDto95.sample()),
            LeafDto95.sample());
    }
}
