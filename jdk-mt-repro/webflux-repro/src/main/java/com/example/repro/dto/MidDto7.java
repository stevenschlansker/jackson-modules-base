package com.example.repro.dto;

import java.util.List;

public record MidDto7(
        LeafDto7 primary7,
        List<LeafDto7> others7,
        int rank7,
        boolean enabled7,
        String note7
) {
    public static MidDto7 sample() {
        return new MidDto7(
            LeafDto7.sample(),
            List.of(LeafDto7.sample(), LeafDto7.sample()),
            19, true, "mid-7");
    }
}
