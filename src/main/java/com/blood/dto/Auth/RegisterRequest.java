package com.blood.dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Họ tên không được để trống")
    @Pattern(regexp = "^[^0-9\\p{Punct}]+$", message = "Họ tên không được chứa chữ số hoặc ký tự đặc biệt")
    private String name;

    @NotBlank(message = "CCCD không được để trống")
    @Pattern(regexp = "^[0-9]{12}$", message = "CCCD phải bao gồm chính xác 12 chữ số")
    private String cccd;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không hợp lệ (phải đúng định dạng nhà mạng VN)")
    private String phone;
}
