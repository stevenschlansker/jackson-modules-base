package com.example.repro.dto;

public record LeafDto59(
        String label59,
        int count59,
        long size59,
        boolean active59,
        boolean verified59,
        Color color59
) {
    public static LeafDto59 sample() {
        return new LeafDto59("leaf-59", 974, 99182L, false, false, Color.AMBER);
    }
}
