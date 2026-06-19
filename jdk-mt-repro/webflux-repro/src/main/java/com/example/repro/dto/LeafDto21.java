package com.example.repro.dto;

public record LeafDto21(
        String label21,
        int count21,
        long size21,
        boolean active21,
        boolean verified21,
        Color color21
) {
    public static LeafDto21 sample() {
        return new LeafDto21("leaf-21", 331, 63398L, false, false, Color.GREEN);
    }
}
