package com.example.repro.dto;

public record LeafDto33(
        String label33,
        int count33,
        long size33,
        boolean active33,
        boolean verified33,
        Color color33
) {
    public static LeafDto33 sample() {
        return new LeafDto33("leaf-33", 575, 93241L, true, true, Color.GREEN);
    }
}
