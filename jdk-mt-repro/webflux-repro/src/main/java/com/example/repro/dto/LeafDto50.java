package com.example.repro.dto;

public record LeafDto50(
        String label50,
        int count50,
        long size50,
        boolean active50,
        boolean verified50,
        Color color50
) {
    public static LeafDto50 sample() {
        return new LeafDto50("leaf-50", 230, 53678L, false, false, Color.BLUE);
    }
}
