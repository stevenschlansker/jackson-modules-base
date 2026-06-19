package com.example.repro.dto;

import java.util.List;

public record MidDto54(
        LeafDto54 primary54,
        List<LeafDto54> others54,
        int rank54,
        boolean enabled54,
        String note54
) {
    public static MidDto54 sample() {
        return new MidDto54(
            LeafDto54.sample(),
            List.of(LeafDto54.sample(), LeafDto54.sample()),
            40, false, "mid-54");
    }
}
