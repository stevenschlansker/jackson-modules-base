package com.example.repro.dto;

public record LeafDto99(
        String label99,
        int count99,
        long size99,
        boolean active99,
        boolean verified99,
        Color color99
) {
    public static LeafDto99 sample() {
        return new LeafDto99("leaf-99", 492, 21949L, false, true, Color.AMBER);
    }
}
