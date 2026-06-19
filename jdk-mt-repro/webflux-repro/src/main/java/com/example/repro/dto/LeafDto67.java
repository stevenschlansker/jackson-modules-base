package com.example.repro.dto;

public record LeafDto67(
        String label67,
        int count67,
        long size67,
        boolean active67,
        boolean verified67,
        Color color67
) {
    public static LeafDto67 sample() {
        return new LeafDto67("leaf-67", 533, 55805L, false, true, Color.AMBER);
    }
}
