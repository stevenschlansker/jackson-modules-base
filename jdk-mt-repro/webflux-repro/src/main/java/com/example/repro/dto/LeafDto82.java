package com.example.repro.dto;

public record LeafDto82(
        String label82,
        int count82,
        long size82,
        boolean active82,
        boolean verified82,
        Color color82
) {
    public static LeafDto82 sample() {
        return new LeafDto82("leaf-82", 628, 9788L, false, false, Color.BLUE);
    }
}
