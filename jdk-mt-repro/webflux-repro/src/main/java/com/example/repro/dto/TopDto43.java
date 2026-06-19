package com.example.repro.dto;

import java.util.List;

public record TopDto43(
        long id43,
        String name43,
        boolean flag43,
        int score43,
        MidDto43 detail43,
        List<MidDto43> children43,
        LeafDto43 extra43
) {
    public static TopDto43 sample() {
        return new TopDto43(
            201029L, "top-43", true, 876,
            MidDto43.sample(),
            List.of(MidDto43.sample(), MidDto43.sample()),
            LeafDto43.sample());
    }
}
