package com.example.repro.dto;

public record LeafDto45(
        String label45,
        int count45,
        long size45,
        boolean active45,
        boolean verified45,
        Color color45
) {
    public static LeafDto45 sample() {
        return new LeafDto45("leaf-45", 833, 73968L, false, true, Color.GREEN);
    }
}
