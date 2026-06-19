package com.example.repro.dto;

import java.util.List;

public record MidDto50(
        LeafDto50 primary50,
        List<LeafDto50> others50,
        int rank50,
        boolean enabled50,
        String note50
) {
    public static MidDto50 sample() {
        return new MidDto50(
            LeafDto50.sample(),
            List.of(LeafDto50.sample(), LeafDto50.sample()),
            47, true, "mid-50");
    }
}
