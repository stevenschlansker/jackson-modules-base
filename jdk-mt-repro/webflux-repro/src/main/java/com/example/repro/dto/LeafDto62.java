package com.example.repro.dto;

public record LeafDto62(
        String label62,
        int count62,
        long size62,
        boolean active62,
        boolean verified62,
        Color color62
) {
    public static LeafDto62 sample() {
        return new LeafDto62("leaf-62", 391, 36967L, true, false, Color.BLUE);
    }
}
