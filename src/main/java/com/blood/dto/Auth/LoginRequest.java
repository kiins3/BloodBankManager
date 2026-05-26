package com.blood.dto.Auth;

import lombok.Getter;
import lombok.Setter;

@Getter
public class LoginRequest {
    String email;
    String password;
}
