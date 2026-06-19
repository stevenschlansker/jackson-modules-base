package com.example.repro.dto;

public record LeafDto95(
        String label95,
        int count95,
        long size95,
        boolean active95,
        boolean verified95,
        Color color95
) {
    public static LeafDto95 sample() {
        return new LeafDto95("leaf-95", 485, 43291L, false, false, Color.AMBER);
    }
}
