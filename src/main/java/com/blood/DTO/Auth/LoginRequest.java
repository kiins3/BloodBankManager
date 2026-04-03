package com.blood.DTO.Auth;

import lombok.Data;

@Data
public class LoginRequest {
    String email;
    String password;
}
