package com.example.repro.dto;

import java.util.List;

public record TopDto3(
        long id3,
        String name3,
        boolean flag3,
        int score3,
        MidDto3 detail3,
        List<MidDto3> children3,
        LeafDto3 extra3
) {
    public static TopDto3 sample() {
        return new TopDto3(
            291085L, "top-3", true, 727,
            MidDto3.sample(),
            List.of(MidDto3.sample(), MidDto3.sample()),
            LeafDto3.sample());
    }
}
