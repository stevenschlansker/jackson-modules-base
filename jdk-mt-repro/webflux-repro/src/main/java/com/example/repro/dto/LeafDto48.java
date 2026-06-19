package com.example.repro.dto;

public record LeafDto48(
        String label48,
        int count48,
        long size48,
        boolean active48,
        boolean verified48,
        Color color48
) {
    public static LeafDto48 sample() {
        return new LeafDto48("leaf-48", 711, 93408L, false, false, Color.RED);
    }
}
