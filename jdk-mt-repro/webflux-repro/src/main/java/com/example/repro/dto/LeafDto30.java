package com.example.repro.dto;

public record LeafDto30(
        String label30,
        int count30,
        long size30,
        boolean active30,
        boolean verified30,
        Color color30
) {
    public static LeafDto30 sample() {
        return new LeafDto30("leaf-30", 294, 66340L, true, false, Color.BLUE);
    }
}
