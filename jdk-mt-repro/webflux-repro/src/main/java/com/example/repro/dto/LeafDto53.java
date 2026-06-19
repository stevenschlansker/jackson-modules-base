package com.example.repro.dto;

public record LeafDto53(
        String label53,
        int count53,
        long size53,
        boolean active53,
        boolean verified53,
        Color color53
) {
    public static LeafDto53 sample() {
        return new LeafDto53("leaf-53", 65, 57425L, true, true, Color.GREEN);
    }
}
