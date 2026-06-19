package com.example.repro.dto;

import java.util.List;

public record TopDto70(
        long id70,
        String name70,
        boolean flag70,
        int score70,
        MidDto70 detail70,
        List<MidDto70> children70,
        LeafDto70 extra70
) {
    public static TopDto70 sample() {
        return new TopDto70(
            98794L, "top-70", false, 759,
            MidDto70.sample(),
            List.of(MidDto70.sample(), MidDto70.sample()),
            LeafDto70.sample());
    }
}
