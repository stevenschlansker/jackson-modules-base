package com.example.repro.dto;

public record LeafDto69(
        String label69,
        int count69,
        long size69,
        boolean active69,
        boolean verified69,
        Color color69
) {
    public static LeafDto69 sample() {
        return new LeafDto69("leaf-69", 128, 5822L, true, false, Color.GREEN);
    }
}
