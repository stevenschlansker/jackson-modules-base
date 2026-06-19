package com.example.repro.dto;

public record LeafDto23(
        String label23,
        int count23,
        long size23,
        boolean active23,
        boolean verified23,
        Color color23
) {
    public static LeafDto23 sample() {
        return new LeafDto23("leaf-23", 991, 84654L, false, true, Color.AMBER);
    }
}
