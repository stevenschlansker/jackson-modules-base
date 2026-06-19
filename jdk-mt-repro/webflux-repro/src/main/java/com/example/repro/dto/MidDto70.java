package com.example.repro.dto;

import java.util.List;

public record MidDto70(
        LeafDto70 primary70,
        List<LeafDto70> others70,
        int rank70,
        boolean enabled70,
        String note70
) {
    public static MidDto70 sample() {
        return new MidDto70(
            LeafDto70.sample(),
            List.of(LeafDto70.sample(), LeafDto70.sample()),
            28, false, "mid-70");
    }
}
