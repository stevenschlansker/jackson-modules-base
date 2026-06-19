package com.example.repro.dto;

public record LeafDto36(
        String label36,
        int count36,
        long size36,
        boolean active36,
        boolean verified36,
        Color color36
) {
    public static LeafDto36 sample() {
        return new LeafDto36("leaf-36", 688, 91518L, false, false, Color.RED);
    }
}
