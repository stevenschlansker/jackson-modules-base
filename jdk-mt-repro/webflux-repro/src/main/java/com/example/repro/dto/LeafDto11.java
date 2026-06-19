package com.example.repro.dto;

public record LeafDto11(
        String label11,
        int count11,
        long size11,
        boolean active11,
        boolean verified11,
        Color color11
) {
    public static LeafDto11 sample() {
        return new LeafDto11("leaf-11", 328, 29870L, false, true, Color.AMBER);
    }
}
