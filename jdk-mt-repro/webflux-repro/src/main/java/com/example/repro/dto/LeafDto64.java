package com.example.repro.dto;

public record LeafDto64(
        String label64,
        int count64,
        long size64,
        boolean active64,
        boolean verified64,
        Color color64
) {
    public static LeafDto64 sample() {
        return new LeafDto64("leaf-64", 514, 36282L, true, true, Color.RED);
    }
}
