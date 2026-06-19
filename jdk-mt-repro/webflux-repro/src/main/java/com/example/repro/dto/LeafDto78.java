package com.example.repro.dto;

public record LeafDto78(
        String label78,
        int count78,
        long size78,
        boolean active78,
        boolean verified78,
        Color color78
) {
    public static LeafDto78 sample() {
        return new LeafDto78("leaf-78", 732, 34567L, false, false, Color.BLUE);
    }
}
