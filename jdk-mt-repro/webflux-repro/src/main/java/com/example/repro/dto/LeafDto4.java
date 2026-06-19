package com.example.repro.dto;

public record LeafDto4(
        String label4,
        int count4,
        long size4,
        boolean active4,
        boolean verified4,
        Color color4
) {
    public static LeafDto4 sample() {
        return new LeafDto4("leaf-4", 212, 8093L, true, false, Color.RED);
    }
}
