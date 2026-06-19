package com.example.repro.dto;

public record LeafDto65(
        String label65,
        int count65,
        long size65,
        boolean active65,
        boolean verified65,
        Color color65
) {
    public static LeafDto65 sample() {
        return new LeafDto65("leaf-65", 64, 10956L, false, false, Color.GREEN);
    }
}
