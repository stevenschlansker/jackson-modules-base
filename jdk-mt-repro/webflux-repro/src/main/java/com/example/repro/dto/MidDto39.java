package com.example.repro.dto;

import java.util.List;

public record MidDto39(
        LeafDto39 primary39,
        List<LeafDto39> others39,
        int rank39,
        boolean enabled39,
        String note39
) {
    public static MidDto39 sample() {
        return new MidDto39(
            LeafDto39.sample(),
            List.of(LeafDto39.sample(), LeafDto39.sample()),
            29, true, "mid-39");
    }
}
