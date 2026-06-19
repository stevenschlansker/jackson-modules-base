package com.example.repro.dto;

public record LeafDto3(
        String label3,
        int count3,
        long size3,
        boolean active3,
        boolean verified3,
        Color color3
) {
    public static LeafDto3 sample() {
        return new LeafDto3("leaf-3", 458, 17387L, false, true, Color.AMBER);
    }
}
