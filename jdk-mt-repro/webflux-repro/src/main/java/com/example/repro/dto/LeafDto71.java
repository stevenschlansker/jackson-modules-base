package com.example.repro.dto;

public record LeafDto71(
        String label71,
        int count71,
        long size71,
        boolean active71,
        boolean verified71,
        Color color71
) {
    public static LeafDto71 sample() {
        return new LeafDto71("leaf-71", 568, 33446L, false, true, Color.AMBER);
    }
}
