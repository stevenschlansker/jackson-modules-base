package com.example.repro.dto;

public record LeafDto26(
        String label26,
        int count26,
        long size26,
        boolean active26,
        boolean verified26,
        Color color26
) {
    public static LeafDto26 sample() {
        return new LeafDto26("leaf-26", 266, 13659L, false, false, Color.BLUE);
    }
}
