package com.example.repro.dto;

public record LeafDto74(
        String label74,
        int count74,
        long size74,
        boolean active74,
        boolean verified74,
        Color color74
) {
    public static LeafDto74 sample() {
        return new LeafDto74("leaf-74", 370, 42109L, true, true, Color.BLUE);
    }
}
