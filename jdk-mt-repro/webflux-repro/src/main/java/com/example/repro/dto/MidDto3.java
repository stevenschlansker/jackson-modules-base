package com.example.repro.dto;

import java.util.List;

public record MidDto3(
        LeafDto3 primary3,
        List<LeafDto3> others3,
        int rank3,
        boolean enabled3,
        String note3
) {
    public static MidDto3 sample() {
        return new MidDto3(
            LeafDto3.sample(),
            List.of(LeafDto3.sample(), LeafDto3.sample()),
            6, false, "mid-3");
    }
}
