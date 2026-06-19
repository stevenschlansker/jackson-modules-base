package com.example.repro.dto;

public record LeafDto90(
        String label90,
        int count90,
        long size90,
        boolean active90,
        boolean verified90,
        Color color90
) {
    public static LeafDto90 sample() {
        return new LeafDto90("leaf-90", 660, 16441L, true, false, Color.BLUE);
    }
}
