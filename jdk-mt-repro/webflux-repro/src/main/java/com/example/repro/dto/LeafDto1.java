package com.example.repro.dto;

public record LeafDto1(
        String label1,
        int count1,
        long size1,
        boolean active1,
        boolean verified1,
        Color color1
) {
    public static LeafDto1 sample() {
        return new LeafDto1("leaf-1", 93, 77182L, false, false, Color.GREEN);
    }
}
