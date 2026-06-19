package com.example.repro.dto;

public record LeafDto88(
        String label88,
        int count88,
        long size88,
        boolean active88,
        boolean verified88,
        Color color88
) {
    public static LeafDto88 sample() {
        return new LeafDto88("leaf-88", 362, 84282L, false, true, Color.RED);
    }
}
