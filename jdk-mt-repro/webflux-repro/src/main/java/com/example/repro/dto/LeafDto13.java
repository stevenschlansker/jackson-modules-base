package com.example.repro.dto;

public record LeafDto13(
        String label13,
        int count13,
        long size13,
        boolean active13,
        boolean verified13,
        Color color13
) {
    public static LeafDto13 sample() {
        return new LeafDto13("leaf-13", 795, 30606L, false, true, Color.GREEN);
    }
}
