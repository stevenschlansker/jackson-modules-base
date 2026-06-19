package com.example.repro.dto;

public record LeafDto27(
        String label27,
        int count27,
        long size27,
        boolean active27,
        boolean verified27,
        Color color27
) {
    public static LeafDto27 sample() {
        return new LeafDto27("leaf-27", 230, 5088L, true, true, Color.AMBER);
    }
}
