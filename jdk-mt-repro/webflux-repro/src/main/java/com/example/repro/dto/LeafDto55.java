package com.example.repro.dto;

public record LeafDto55(
        String label55,
        int count55,
        long size55,
        boolean active55,
        boolean verified55,
        Color color55
) {
    public static LeafDto55 sample() {
        return new LeafDto55("leaf-55", 721, 69074L, false, false, Color.AMBER);
    }
}
