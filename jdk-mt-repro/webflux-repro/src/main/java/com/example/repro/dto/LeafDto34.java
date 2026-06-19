package com.example.repro.dto;

public record LeafDto34(
        String label34,
        int count34,
        long size34,
        boolean active34,
        boolean verified34,
        Color color34
) {
    public static LeafDto34 sample() {
        return new LeafDto34("leaf-34", 913, 58633L, false, false, Color.BLUE);
    }
}
