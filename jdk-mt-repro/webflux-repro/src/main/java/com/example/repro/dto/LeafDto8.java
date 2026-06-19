package com.example.repro.dto;

public record LeafDto8(
        String label8,
        int count8,
        long size8,
        boolean active8,
        boolean verified8,
        Color color8
) {
    public static LeafDto8 sample() {
        return new LeafDto8("leaf-8", 453, 10429L, false, true, Color.RED);
    }
}
