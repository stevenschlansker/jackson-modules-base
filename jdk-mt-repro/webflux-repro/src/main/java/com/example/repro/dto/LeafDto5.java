package com.example.repro.dto;

public record LeafDto5(
        String label5,
        int count5,
        long size5,
        boolean active5,
        boolean verified5,
        Color color5
) {
    public static LeafDto5 sample() {
        return new LeafDto5("leaf-5", 534, 2807L, true, false, Color.GREEN);
    }
}
