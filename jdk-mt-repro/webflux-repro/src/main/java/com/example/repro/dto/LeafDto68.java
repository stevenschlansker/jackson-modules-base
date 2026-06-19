package com.example.repro.dto;

public record LeafDto68(
        String label68,
        int count68,
        long size68,
        boolean active68,
        boolean verified68,
        Color color68
) {
    public static LeafDto68 sample() {
        return new LeafDto68("leaf-68", 503, 67629L, false, false, Color.RED);
    }
}
