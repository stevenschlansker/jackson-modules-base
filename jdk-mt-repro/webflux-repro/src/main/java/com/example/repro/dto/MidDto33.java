package com.example.repro.dto;

import java.util.List;

public record MidDto33(
        LeafDto33 primary33,
        List<LeafDto33> others33,
        int rank33,
        boolean enabled33,
        String note33
) {
    public static MidDto33 sample() {
        return new MidDto33(
            LeafDto33.sample(),
            List.of(LeafDto33.sample(), LeafDto33.sample()),
            39, false, "mid-33");
    }
}
