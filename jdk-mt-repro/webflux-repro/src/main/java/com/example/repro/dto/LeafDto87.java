package com.example.repro.dto;

public record LeafDto87(
        String label87,
        int count87,
        long size87,
        boolean active87,
        boolean verified87,
        Color color87
) {
    public static LeafDto87 sample() {
        return new LeafDto87("leaf-87", 666, 50210L, true, true, Color.AMBER);
    }
}
