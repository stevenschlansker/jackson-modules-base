package com.example.repro.dto;

public record LeafDto72(
        String label72,
        int count72,
        long size72,
        boolean active72,
        boolean verified72,
        Color color72
) {
    public static LeafDto72 sample() {
        return new LeafDto72("leaf-72", 788, 22578L, false, true, Color.RED);
    }
}
