package com.example.repro.dto;

public record LeafDto25(
        String label25,
        int count25,
        long size25,
        boolean active25,
        boolean verified25,
        Color color25
) {
    public static LeafDto25 sample() {
        return new LeafDto25("leaf-25", 553, 3416L, true, true, Color.GREEN);
    }
}
