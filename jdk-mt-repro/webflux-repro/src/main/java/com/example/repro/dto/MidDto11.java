package com.example.repro.dto;

import java.util.List;

public record MidDto11(
        LeafDto11 primary11,
        List<LeafDto11> others11,
        int rank11,
        boolean enabled11,
        String note11
) {
    public static MidDto11 sample() {
        return new MidDto11(
            LeafDto11.sample(),
            List.of(LeafDto11.sample(), LeafDto11.sample()),
            29, true, "mid-11");
    }
}
