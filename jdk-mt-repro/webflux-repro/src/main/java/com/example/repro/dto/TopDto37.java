package com.example.repro.dto;

import java.util.List;

public record TopDto37(
        long id37,
        String name37,
        boolean flag37,
        int score37,
        MidDto37 detail37,
        List<MidDto37> children37,
        LeafDto37 extra37
) {
    public static TopDto37 sample() {
        return new TopDto37(
            625386L, "top-37", true, 794,
            MidDto37.sample(),
            List.of(MidDto37.sample(), MidDto37.sample()),
            LeafDto37.sample());
    }
}
