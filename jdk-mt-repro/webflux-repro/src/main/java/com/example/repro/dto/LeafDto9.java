package com.example.repro.dto;

public record LeafDto9(
        String label9,
        int count9,
        long size9,
        boolean active9,
        boolean verified9,
        Color color9
) {
    public static LeafDto9 sample() {
        return new LeafDto9("leaf-9", 594, 61403L, false, false, Color.GREEN);
    }
}
