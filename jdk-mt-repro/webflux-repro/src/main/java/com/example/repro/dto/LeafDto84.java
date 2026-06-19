package com.example.repro.dto;

public record LeafDto84(
        String label84,
        int count84,
        long size84,
        boolean active84,
        boolean verified84,
        Color color84
) {
    public static LeafDto84 sample() {
        return new LeafDto84("leaf-84", 349, 43977L, false, true, Color.RED);
    }
}
