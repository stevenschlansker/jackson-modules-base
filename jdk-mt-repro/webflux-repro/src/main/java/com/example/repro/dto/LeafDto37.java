package com.example.repro.dto;

public record LeafDto37(
        String label37,
        int count37,
        long size37,
        boolean active37,
        boolean verified37,
        Color color37
) {
    public static LeafDto37 sample() {
        return new LeafDto37("leaf-37", 137, 75036L, false, true, Color.GREEN);
    }
}
