package com.blood.dto.Auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String tokenType = "Bearer";

    public JwtResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}