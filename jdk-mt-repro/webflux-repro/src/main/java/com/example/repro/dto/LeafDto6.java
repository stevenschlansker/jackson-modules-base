package com.example.repro.dto;

public record LeafDto6(
        String label6,
        int count6,
        long size6,
        boolean active6,
        boolean verified6,
        Color color6
) {
    public static LeafDto6 sample() {
        return new LeafDto6("leaf-6", 160, 64410L, true, false, Color.BLUE);
    }
}
