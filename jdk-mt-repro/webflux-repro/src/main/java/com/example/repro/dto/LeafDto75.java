package com.example.repro.dto;

public record LeafDto75(
        String label75,
        int count75,
        long size75,
        boolean active75,
        boolean verified75,
        Color color75
) {
    public static LeafDto75 sample() {
        return new LeafDto75("leaf-75", 732, 89428L, true, true, Color.AMBER);
    }
}
