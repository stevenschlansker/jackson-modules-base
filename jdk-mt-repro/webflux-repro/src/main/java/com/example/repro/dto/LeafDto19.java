package com.example.repro.dto;

public record LeafDto19(
        String label19,
        int count19,
        long size19,
        boolean active19,
        boolean verified19,
        Color color19
) {
    public static LeafDto19 sample() {
        return new LeafDto19("leaf-19", 434, 28741L, false, false, Color.AMBER);
    }
}
