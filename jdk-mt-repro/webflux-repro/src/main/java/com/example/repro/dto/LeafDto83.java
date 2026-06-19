package com.example.repro.dto;

public record LeafDto83(
        String label83,
        int count83,
        long size83,
        boolean active83,
        boolean verified83,
        Color color83
) {
    public static LeafDto83 sample() {
        return new LeafDto83("leaf-83", 749, 99778L, true, false, Color.AMBER);
    }
}
