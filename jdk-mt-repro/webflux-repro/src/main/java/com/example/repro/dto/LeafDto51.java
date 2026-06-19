package com.example.repro.dto;

public record LeafDto51(
        String label51,
        int count51,
        long size51,
        boolean active51,
        boolean verified51,
        Color color51
) {
    public static LeafDto51 sample() {
        return new LeafDto51("leaf-51", 125, 82281L, true, false, Color.AMBER);
    }
}
