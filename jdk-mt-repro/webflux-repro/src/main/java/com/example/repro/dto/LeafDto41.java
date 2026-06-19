package com.example.repro.dto;

public record LeafDto41(
        String label41,
        int count41,
        long size41,
        boolean active41,
        boolean verified41,
        Color color41
) {
    public static LeafDto41 sample() {
        return new LeafDto41("leaf-41", 380, 60895L, false, true, Color.GREEN);
    }
}
