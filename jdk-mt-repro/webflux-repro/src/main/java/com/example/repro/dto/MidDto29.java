package com.example.repro.dto;

import java.util.List;

public record MidDto29(
        LeafDto29 primary29,
        List<LeafDto29> others29,
        int rank29,
        boolean enabled29,
        String note29
) {
    public static MidDto29 sample() {
        return new MidDto29(
            LeafDto29.sample(),
            List.of(LeafDto29.sample(), LeafDto29.sample()),
            47, true, "mid-29");
    }
}
