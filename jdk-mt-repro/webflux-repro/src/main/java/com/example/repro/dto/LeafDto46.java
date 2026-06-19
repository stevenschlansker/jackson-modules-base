package com.example.repro.dto;

public record LeafDto46(
        String label46,
        int count46,
        long size46,
        boolean active46,
        boolean verified46,
        Color color46
) {
    public static LeafDto46 sample() {
        return new LeafDto46("leaf-46", 208, 27094L, true, false, Color.BLUE);
    }
}
