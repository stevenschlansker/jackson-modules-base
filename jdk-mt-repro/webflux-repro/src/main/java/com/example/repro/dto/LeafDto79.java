package com.example.repro.dto;

public record LeafDto79(
        String label79,
        int count79,
        long size79,
        boolean active79,
        boolean verified79,
        Color color79
) {
    public static LeafDto79 sample() {
        return new LeafDto79("leaf-79", 218, 78049L, true, true, Color.AMBER);
    }
}
