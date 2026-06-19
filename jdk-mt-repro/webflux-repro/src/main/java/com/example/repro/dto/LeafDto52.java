package com.example.repro.dto;

public record LeafDto52(
        String label52,
        int count52,
        long size52,
        boolean active52,
        boolean verified52,
        Color color52
) {
    public static LeafDto52 sample() {
        return new LeafDto52("leaf-52", 599, 85746L, true, false, Color.RED);
    }
}
