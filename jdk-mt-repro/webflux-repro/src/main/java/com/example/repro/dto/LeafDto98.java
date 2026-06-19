package com.example.repro.dto;

public record LeafDto98(
        String label98,
        int count98,
        long size98,
        boolean active98,
        boolean verified98,
        Color color98
) {
    public static LeafDto98 sample() {
        return new LeafDto98("leaf-98", 85, 32882L, false, true, Color.BLUE);
    }
}
