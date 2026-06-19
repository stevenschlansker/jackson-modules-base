package com.example.repro.dto;

public record LeafDto49(
        String label49,
        int count49,
        long size49,
        boolean active49,
        boolean verified49,
        Color color49
) {
    public static LeafDto49 sample() {
        return new LeafDto49("leaf-49", 438, 3391L, true, false, Color.GREEN);
    }
}
