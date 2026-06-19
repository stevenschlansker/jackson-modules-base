package com.example.repro.dto;

public record LeafDto32(
        String label32,
        int count32,
        long size32,
        boolean active32,
        boolean verified32,
        Color color32
) {
    public static LeafDto32 sample() {
        return new LeafDto32("leaf-32", 725, 74590L, false, false, Color.RED);
    }
}
