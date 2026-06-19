package com.example.repro.dto;

public record LeafDto89(
        String label89,
        int count89,
        long size89,
        boolean active89,
        boolean verified89,
        Color color89
) {
    public static LeafDto89 sample() {
        return new LeafDto89("leaf-89", 271, 63029L, true, false, Color.GREEN);
    }
}
