package com.example.repro.dto;

public record LeafDto43(
        String label43,
        int count43,
        long size43,
        boolean active43,
        boolean verified43,
        Color color43
) {
    public static LeafDto43 sample() {
        return new LeafDto43("leaf-43", 145, 76515L, true, true, Color.AMBER);
    }
}
