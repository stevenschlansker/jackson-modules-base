package com.example.repro.dto;

import java.util.List;

public record MidDto51(
        LeafDto51 primary51,
        List<LeafDto51> others51,
        int rank51,
        boolean enabled51,
        String note51
) {
    public static MidDto51 sample() {
        return new MidDto51(
            LeafDto51.sample(),
            List.of(LeafDto51.sample(), LeafDto51.sample()),
            9, false, "mid-51");
    }
}
