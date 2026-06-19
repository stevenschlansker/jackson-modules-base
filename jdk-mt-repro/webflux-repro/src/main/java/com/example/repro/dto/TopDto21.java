package com.example.repro.dto;

import java.util.List;

public record TopDto21(
        long id21,
        String name21,
        boolean flag21,
        int score21,
        MidDto21 detail21,
        List<MidDto21> children21,
        LeafDto21 extra21
) {
    public static TopDto21 sample() {
        return new TopDto21(
            569379L, "top-21", false, 943,
            MidDto21.sample(),
            List.of(MidDto21.sample(), MidDto21.sample()),
            LeafDto21.sample());
    }
}
