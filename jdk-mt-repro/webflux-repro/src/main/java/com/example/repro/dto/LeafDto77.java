package com.example.repro.dto;

public record LeafDto77(
        String label77,
        int count77,
        long size77,
        boolean active77,
        boolean verified77,
        Color color77
) {
    public static LeafDto77 sample() {
        return new LeafDto77("leaf-77", 712, 35024L, false, false, Color.GREEN);
    }
}
