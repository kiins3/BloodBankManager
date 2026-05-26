package com.blood.model.enumformat;

public enum AssignmentRole {
    TIEP_DON("Tiếp đón"),
    KHAM_SANG_LOC("Khám sàng lọc"),
    LAY_MAU("Lấy máu");

    private final String displayName;

    AssignmentRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
