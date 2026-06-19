package com.example.repro.dto;

public record LeafDto14(
        String label14,
        int count14,
        long size14,
        boolean active14,
        boolean verified14,
        Color color14
) {
    public static LeafDto14 sample() {
        return new LeafDto14("leaf-14", 322, 17923L, true, true, Color.BLUE);
    }
}
