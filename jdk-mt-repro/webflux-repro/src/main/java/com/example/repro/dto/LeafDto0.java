package com.example.repro.dto;

public record LeafDto0(
        String label0,
        int count0,
        long size0,
        boolean active0,
        boolean verified0,
        Color color0
) {
    public static LeafDto0 sample() {
        return new LeafDto0("leaf-0", 130, 92763L, true, false, Color.RED);
    }
}
