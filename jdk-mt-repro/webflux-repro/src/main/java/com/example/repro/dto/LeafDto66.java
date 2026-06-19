package com.example.repro.dto;

public record LeafDto66(
        String label66,
        int count66,
        long size66,
        boolean active66,
        boolean verified66,
        Color color66
) {
    public static LeafDto66 sample() {
        return new LeafDto66("leaf-66", 572, 82424L, false, false, Color.BLUE);
    }
}
