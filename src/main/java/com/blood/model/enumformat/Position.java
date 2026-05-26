package com.blood.model.enumformat;

public enum Position {
    BAC_SI("Bác sĩ"),
    Y_TA("Y tá"),
    KY_THUAT("Kỹ thuật"),
    QUAN_LY_KHO ("Quản lý kho"),
    ADMIN("Admin");

    private final String displayName;

    Position(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
