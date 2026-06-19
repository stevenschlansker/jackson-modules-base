package com.example.repro.dto;

public record LeafDto42(
        String label42,
        int count42,
        long size42,
        boolean active42,
        boolean verified42,
        Color color42
) {
    public static LeafDto42 sample() {
        return new LeafDto42("leaf-42", 165, 23588L, false, false, Color.BLUE);
    }
}
