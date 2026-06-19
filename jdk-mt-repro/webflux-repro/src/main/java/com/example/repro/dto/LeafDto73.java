package com.example.repro.dto;

public record LeafDto73(
        String label73,
        int count73,
        long size73,
        boolean active73,
        boolean verified73,
        Color color73
) {
    public static LeafDto73 sample() {
        return new LeafDto73("leaf-73", 809, 42221L, true, true, Color.GREEN);
    }
}
