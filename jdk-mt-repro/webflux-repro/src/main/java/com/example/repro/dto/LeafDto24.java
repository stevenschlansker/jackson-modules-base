package com.example.repro.dto;

public record LeafDto24(
        String label24,
        int count24,
        long size24,
        boolean active24,
        boolean verified24,
        Color color24
) {
    public static LeafDto24 sample() {
        return new LeafDto24("leaf-24", 359, 43731L, true, true, Color.RED);
    }
}
