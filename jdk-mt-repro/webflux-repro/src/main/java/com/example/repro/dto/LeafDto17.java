package com.example.repro.dto;

public record LeafDto17(
        String label17,
        int count17,
        long size17,
        boolean active17,
        boolean verified17,
        Color color17
) {
    public static LeafDto17 sample() {
        return new LeafDto17("leaf-17", 755, 45481L, true, true, Color.GREEN);
    }
}
