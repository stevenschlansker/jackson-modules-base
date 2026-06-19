package com.example.repro.dto;

public record LeafDto85(
        String label85,
        int count85,
        long size85,
        boolean active85,
        boolean verified85,
        Color color85
) {
    public static LeafDto85 sample() {
        return new LeafDto85("leaf-85", 630, 6364L, false, false, Color.GREEN);
    }
}
