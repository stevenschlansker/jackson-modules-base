package com.example.repro.dto;

public record LeafDto94(
        String label94,
        int count94,
        long size94,
        boolean active94,
        boolean verified94,
        Color color94
) {
    public static LeafDto94 sample() {
        return new LeafDto94("leaf-94", 480, 36739L, true, true, Color.BLUE);
    }
}
