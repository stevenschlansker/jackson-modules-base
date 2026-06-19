package com.example.repro.dto;

import java.util.List;

public record MidDto14(
        LeafDto14 primary14,
        List<LeafDto14> others14,
        int rank14,
        boolean enabled14,
        String note14
) {
    public static MidDto14 sample() {
        return new MidDto14(
            LeafDto14.sample(),
            List.of(LeafDto14.sample(), LeafDto14.sample()),
            2, true, "mid-14");
    }
}
