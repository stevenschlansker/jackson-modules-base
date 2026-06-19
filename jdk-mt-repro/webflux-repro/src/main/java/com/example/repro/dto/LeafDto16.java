package com.example.repro.dto;

public record LeafDto16(
        String label16,
        int count16,
        long size16,
        boolean active16,
        boolean verified16,
        Color color16
) {
    public static LeafDto16 sample() {
        return new LeafDto16("leaf-16", 904, 64803L, false, false, Color.RED);
    }
}
