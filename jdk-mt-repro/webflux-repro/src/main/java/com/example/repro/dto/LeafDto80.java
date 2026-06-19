package com.example.repro.dto;

public record LeafDto80(
        String label80,
        int count80,
        long size80,
        boolean active80,
        boolean verified80,
        Color color80
) {
    public static LeafDto80 sample() {
        return new LeafDto80("leaf-80", 795, 47935L, false, false, Color.RED);
    }
}
