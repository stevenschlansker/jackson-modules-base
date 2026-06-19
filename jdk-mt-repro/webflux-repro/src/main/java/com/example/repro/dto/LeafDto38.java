package com.example.repro.dto;

public record LeafDto38(
        String label38,
        int count38,
        long size38,
        boolean active38,
        boolean verified38,
        Color color38
) {
    public static LeafDto38 sample() {
        return new LeafDto38("leaf-38", 7, 82056L, true, false, Color.BLUE);
    }
}
