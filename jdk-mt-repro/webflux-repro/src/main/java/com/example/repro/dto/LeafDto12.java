package com.example.repro.dto;

public record LeafDto12(
        String label12,
        int count12,
        long size12,
        boolean active12,
        boolean verified12,
        Color color12
) {
    public static LeafDto12 sample() {
        return new LeafDto12("leaf-12", 275, 52522L, false, false, Color.RED);
    }
}
