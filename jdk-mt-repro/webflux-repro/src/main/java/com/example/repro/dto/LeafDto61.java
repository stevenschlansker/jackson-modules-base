package com.example.repro.dto;

public record LeafDto61(
        String label61,
        int count61,
        long size61,
        boolean active61,
        boolean verified61,
        Color color61
) {
    public static LeafDto61 sample() {
        return new LeafDto61("leaf-61", 825, 2848L, true, false, Color.GREEN);
    }
}
