package com.example.repro.dto;

public record LeafDto93(
        String label93,
        int count93,
        long size93,
        boolean active93,
        boolean verified93,
        Color color93
) {
    public static LeafDto93 sample() {
        return new LeafDto93("leaf-93", 0, 50601L, false, false, Color.GREEN);
    }
}
