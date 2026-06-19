package com.example.repro.dto;

public record LeafDto96(
        String label96,
        int count96,
        long size96,
        boolean active96,
        boolean verified96,
        Color color96
) {
    public static LeafDto96 sample() {
        return new LeafDto96("leaf-96", 646, 6050L, true, false, Color.RED);
    }
}
