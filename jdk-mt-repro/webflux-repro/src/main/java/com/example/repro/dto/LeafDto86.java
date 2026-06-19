package com.example.repro.dto;

public record LeafDto86(
        String label86,
        int count86,
        long size86,
        boolean active86,
        boolean verified86,
        Color color86
) {
    public static LeafDto86 sample() {
        return new LeafDto86("leaf-86", 67, 95017L, false, true, Color.BLUE);
    }
}
