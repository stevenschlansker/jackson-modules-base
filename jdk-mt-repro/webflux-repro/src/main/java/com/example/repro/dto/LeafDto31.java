package com.example.repro.dto;

public record LeafDto31(
        String label31,
        int count31,
        long size31,
        boolean active31,
        boolean verified31,
        Color color31
) {
    public static LeafDto31 sample() {
        return new LeafDto31("leaf-31", 842, 91067L, false, true, Color.AMBER);
    }
}
