package com.example.repro.dto;

public record LeafDto91(
        String label91,
        int count91,
        long size91,
        boolean active91,
        boolean verified91,
        Color color91
) {
    public static LeafDto91 sample() {
        return new LeafDto91("leaf-91", 264, 50697L, true, true, Color.AMBER);
    }
}
