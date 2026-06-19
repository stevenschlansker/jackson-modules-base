package com.example.repro.dto;

public record LeafDto15(
        String label15,
        int count15,
        long size15,
        boolean active15,
        boolean verified15,
        Color color15
) {
    public static LeafDto15 sample() {
        return new LeafDto15("leaf-15", 168, 90525L, true, false, Color.AMBER);
    }
}
