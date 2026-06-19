package com.example.repro.dto;

public record LeafDto40(
        String label40,
        int count40,
        long size40,
        boolean active40,
        boolean verified40,
        Color color40
) {
    public static LeafDto40 sample() {
        return new LeafDto40("leaf-40", 923, 92577L, true, false, Color.RED);
    }
}
