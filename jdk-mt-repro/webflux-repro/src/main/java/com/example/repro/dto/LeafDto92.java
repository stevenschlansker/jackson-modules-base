package com.example.repro.dto;

public record LeafDto92(
        String label92,
        int count92,
        long size92,
        boolean active92,
        boolean verified92,
        Color color92
) {
    public static LeafDto92 sample() {
        return new LeafDto92("leaf-92", 948, 87871L, true, true, Color.RED);
    }
}
