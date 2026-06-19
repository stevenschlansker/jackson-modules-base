package com.example.repro.dto;

import java.util.List;

public record TopDto61(
        long id61,
        String name61,
        boolean flag61,
        int score61,
        MidDto61 detail61,
        List<MidDto61> children61,
        LeafDto61 extra61
) {
    public static TopDto61 sample() {
        return new TopDto61(
            292492L, "top-61", false, 87,
            MidDto61.sample(),
            List.of(MidDto61.sample(), MidDto61.sample()),
            LeafDto61.sample());
    }
}
