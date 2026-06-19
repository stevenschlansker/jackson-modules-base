package com.example.repro.dto;

public record LeafDto44(
        String label44,
        int count44,
        long size44,
        boolean active44,
        boolean verified44,
        Color color44
) {
    public static LeafDto44 sample() {
        return new LeafDto44("leaf-44", 717, 4546L, false, true, Color.RED);
    }
}
