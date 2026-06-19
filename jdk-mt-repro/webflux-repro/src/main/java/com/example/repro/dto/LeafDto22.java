package com.example.repro.dto;

public record LeafDto22(
        String label22,
        int count22,
        long size22,
        boolean active22,
        boolean verified22,
        Color color22
) {
    public static LeafDto22 sample() {
        return new LeafDto22("leaf-22", 793, 63374L, false, false, Color.BLUE);
    }
}
