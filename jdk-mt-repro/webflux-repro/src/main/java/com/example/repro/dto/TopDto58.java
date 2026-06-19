package com.example.repro.dto;

import java.util.List;

public record TopDto58(
        long id58,
        String name58,
        boolean flag58,
        int score58,
        MidDto58 detail58,
        List<MidDto58> children58,
        LeafDto58 extra58
) {
    public static TopDto58 sample() {
        return new TopDto58(
            856338L, "top-58", true, 664,
            MidDto58.sample(),
            List.of(MidDto58.sample(), MidDto58.sample()),
            LeafDto58.sample());
    }
}
