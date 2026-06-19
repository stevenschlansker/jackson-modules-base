package com.example.repro.dto;

public record LeafDto70(
        String label70,
        int count70,
        long size70,
        boolean active70,
        boolean verified70,
        Color color70
) {
    public static LeafDto70 sample() {
        return new LeafDto70("leaf-70", 451, 12838L, false, false, Color.BLUE);
    }
}
