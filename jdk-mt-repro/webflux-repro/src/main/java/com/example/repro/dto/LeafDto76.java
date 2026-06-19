package com.example.repro.dto;

public record LeafDto76(
        String label76,
        int count76,
        long size76,
        boolean active76,
        boolean verified76,
        Color color76
) {
    public static LeafDto76 sample() {
        return new LeafDto76("leaf-76", 186, 24431L, true, true, Color.RED);
    }
}
