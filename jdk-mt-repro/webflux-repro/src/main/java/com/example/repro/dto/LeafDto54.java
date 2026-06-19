package com.example.repro.dto;

public record LeafDto54(
        String label54,
        int count54,
        long size54,
        boolean active54,
        boolean verified54,
        Color color54
) {
    public static LeafDto54 sample() {
        return new LeafDto54("leaf-54", 786, 9254L, true, true, Color.BLUE);
    }
}
