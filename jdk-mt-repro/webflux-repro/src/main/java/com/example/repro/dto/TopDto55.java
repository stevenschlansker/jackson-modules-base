package com.example.repro.dto;

import java.util.List;

public record TopDto55(
        long id55,
        String name55,
        boolean flag55,
        int score55,
        MidDto55 detail55,
        List<MidDto55> children55,
        LeafDto55 extra55
) {
    public static TopDto55 sample() {
        return new TopDto55(
            762676L, "top-55", false, 823,
            MidDto55.sample(),
            List.of(MidDto55.sample(), MidDto55.sample()),
            LeafDto55.sample());
    }
}
