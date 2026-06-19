package com.example.repro.dto;

import java.util.List;

public record MidDto80(
        LeafDto80 primary80,
        List<LeafDto80> others80,
        int rank80,
        boolean enabled80,
        String note80
) {
    public static MidDto80 sample() {
        return new MidDto80(
            LeafDto80.sample(),
            List.of(LeafDto80.sample(), LeafDto80.sample()),
            45, false, "mid-80");
    }
}
