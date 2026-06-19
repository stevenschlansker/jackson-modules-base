package com.example.repro.dto;

public record LeafDto56(
        String label56,
        int count56,
        long size56,
        boolean active56,
        boolean verified56,
        Color color56
) {
    public static LeafDto56 sample() {
        return new LeafDto56("leaf-56", 150, 28970L, true, false, Color.RED);
    }
}
