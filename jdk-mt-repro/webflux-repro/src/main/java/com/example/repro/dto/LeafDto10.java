package com.example.repro.dto;

public record LeafDto10(
        String label10,
        int count10,
        long size10,
        boolean active10,
        boolean verified10,
        Color color10
) {
    public static LeafDto10 sample() {
        return new LeafDto10("leaf-10", 747, 77696L, false, false, Color.BLUE);
    }
}
