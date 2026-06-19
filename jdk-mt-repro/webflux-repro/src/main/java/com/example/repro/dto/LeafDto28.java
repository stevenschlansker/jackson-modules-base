package com.example.repro.dto;

public record LeafDto28(
        String label28,
        int count28,
        long size28,
        boolean active28,
        boolean verified28,
        Color color28
) {
    public static LeafDto28 sample() {
        return new LeafDto28("leaf-28", 881, 89425L, false, false, Color.RED);
    }
}
