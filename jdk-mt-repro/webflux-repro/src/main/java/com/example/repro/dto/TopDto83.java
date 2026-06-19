package com.example.repro.dto;

import java.util.List;

public record TopDto83(
        long id83,
        String name83,
        boolean flag83,
        int score83,
        MidDto83 detail83,
        List<MidDto83> children83,
        LeafDto83 extra83
) {
    public static TopDto83 sample() {
        return new TopDto83(
            773316L, "top-83", true, 361,
            MidDto83.sample(),
            List.of(MidDto83.sample(), MidDto83.sample()),
            LeafDto83.sample());
    }
}
