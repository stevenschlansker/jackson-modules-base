package com.example.repro.dto;

public record LeafDto47(
        String label47,
        int count47,
        long size47,
        boolean active47,
        boolean verified47,
        Color color47
) {
    public static LeafDto47 sample() {
        return new LeafDto47("leaf-47", 95, 32974L, true, false, Color.AMBER);
    }
}
