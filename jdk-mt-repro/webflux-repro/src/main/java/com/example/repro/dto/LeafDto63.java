package com.example.repro.dto;

public record LeafDto63(
        String label63,
        int count63,
        long size63,
        boolean active63,
        boolean verified63,
        Color color63
) {
    public static LeafDto63 sample() {
        return new LeafDto63("leaf-63", 557, 4657L, false, false, Color.AMBER);
    }
}
