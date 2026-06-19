package com.example.repro.dto;

public record LeafDto97(
        String label97,
        int count97,
        long size97,
        boolean active97,
        boolean verified97,
        Color color97
) {
    public static LeafDto97 sample() {
        return new LeafDto97("leaf-97", 906, 84284L, false, false, Color.GREEN);
    }
}
