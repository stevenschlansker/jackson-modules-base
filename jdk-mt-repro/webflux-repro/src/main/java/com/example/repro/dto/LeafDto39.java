package com.example.repro.dto;

public record LeafDto39(
        String label39,
        int count39,
        long size39,
        boolean active39,
        boolean verified39,
        Color color39
) {
    public static LeafDto39 sample() {
        return new LeafDto39("leaf-39", 557, 93241L, false, true, Color.AMBER);
    }
}
