package com.example.repro.dto;

public record LeafDto20(
        String label20,
        int count20,
        long size20,
        boolean active20,
        boolean verified20,
        Color color20
) {
    public static LeafDto20 sample() {
        return new LeafDto20("leaf-20", 46, 53589L, false, false, Color.RED);
    }
}
