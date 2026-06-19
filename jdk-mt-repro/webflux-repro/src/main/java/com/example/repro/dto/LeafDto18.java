package com.example.repro.dto;

public record LeafDto18(
        String label18,
        int count18,
        long size18,
        boolean active18,
        boolean verified18,
        Color color18
) {
    public static LeafDto18 sample() {
        return new LeafDto18("leaf-18", 740, 30610L, true, true, Color.BLUE);
    }
}
