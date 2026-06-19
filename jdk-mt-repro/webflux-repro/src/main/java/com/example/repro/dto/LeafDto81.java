package com.example.repro.dto;

public record LeafDto81(
        String label81,
        int count81,
        long size81,
        boolean active81,
        boolean verified81,
        Color color81
) {
    public static LeafDto81 sample() {
        return new LeafDto81("leaf-81", 544, 12304L, false, true, Color.GREEN);
    }
}
