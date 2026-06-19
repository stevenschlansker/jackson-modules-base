package com.example.repro.dto;

import java.util.List;

public record MidDto93(
        LeafDto93 primary93,
        List<LeafDto93> others93,
        int rank93,
        boolean enabled93,
        String note93
) {
    public static MidDto93 sample() {
        return new MidDto93(
            LeafDto93.sample(),
            List.of(LeafDto93.sample(), LeafDto93.sample()),
            22, true, "mid-93");
    }
}
