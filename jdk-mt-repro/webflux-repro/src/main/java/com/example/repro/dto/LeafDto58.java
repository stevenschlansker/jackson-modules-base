package com.example.repro.dto;

public record LeafDto58(
        String label58,
        int count58,
        long size58,
        boolean active58,
        boolean verified58,
        Color color58
) {
    public static LeafDto58 sample() {
        return new LeafDto58("leaf-58", 725, 22051L, true, false, Color.BLUE);
    }
}
