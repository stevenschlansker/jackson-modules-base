package com.example.repro.dto;

public record LeafDto7(
        String label7,
        int count7,
        long size7,
        boolean active7,
        boolean verified7,
        Color color7
) {
    public static LeafDto7 sample() {
        return new LeafDto7("leaf-7", 601, 9094L, true, true, Color.AMBER);
    }
}
