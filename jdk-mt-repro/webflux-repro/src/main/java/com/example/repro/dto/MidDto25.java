package com.example.repro.dto;

import java.util.List;

public record MidDto25(
        LeafDto25 primary25,
        List<LeafDto25> others25,
        int rank25,
        boolean enabled25,
        String note25
) {
    public static MidDto25 sample() {
        return new MidDto25(
            LeafDto25.sample(),
            List.of(LeafDto25.sample(), LeafDto25.sample()),
            37, false, "mid-25");
    }
}
