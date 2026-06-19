package com.example.repro.dto;

public record LeafDto60(
        String label60,
        int count60,
        long size60,
        boolean active60,
        boolean verified60,
        Color color60
) {
    public static LeafDto60 sample() {
        return new LeafDto60("leaf-60", 100, 77001L, true, false, Color.RED);
    }
}
