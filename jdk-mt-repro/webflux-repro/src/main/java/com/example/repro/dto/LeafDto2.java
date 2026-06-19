package com.example.repro.dto;

public record LeafDto2(
        String label2,
        int count2,
        long size2,
        boolean active2,
        boolean verified2,
        Color color2
) {
    public static LeafDto2 sample() {
        return new LeafDto2("leaf-2", 743, 72209L, true, false, Color.BLUE);
    }
}
