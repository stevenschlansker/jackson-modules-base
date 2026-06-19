package com.example.repro.dto;

public record LeafDto57(
        String label57,
        int count57,
        long size57,
        boolean active57,
        boolean verified57,
        Color color57
) {
    public static LeafDto57 sample() {
        return new LeafDto57("leaf-57", 934, 72721L, true, true, Color.GREEN);
    }
}
