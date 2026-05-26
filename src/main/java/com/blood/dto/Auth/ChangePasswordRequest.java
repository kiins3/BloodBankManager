package com.blood.dto.Auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Mật khẩu không được để trống")
    private String oldPassword;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$", message = "Mật khẩu phải có ít nhất 6 ký tự, bao gồm cả chữ cái và số")
    private String newPassword;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String confirmPassword;
}
