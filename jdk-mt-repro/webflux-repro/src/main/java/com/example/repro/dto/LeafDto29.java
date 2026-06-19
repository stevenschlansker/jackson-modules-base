package com.example.repro.dto;

public record LeafDto29(
        String label29,
        int count29,
        long size29,
        boolean active29,
        boolean verified29,
        Color color29
) {
    public static LeafDto29 sample() {
        return new LeafDto29("leaf-29", 509, 56760L, false, true, Color.GREEN);
    }
}
