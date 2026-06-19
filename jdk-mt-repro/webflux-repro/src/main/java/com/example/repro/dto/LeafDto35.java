package com.example.repro.dto;

public record LeafDto35(
        String label35,
        int count35,
        long size35,
        boolean active35,
        boolean verified35,
        Color color35
) {
    public static LeafDto35 sample() {
        return new LeafDto35("leaf-35", 694, 53955L, false, false, Color.AMBER);
    }
}
