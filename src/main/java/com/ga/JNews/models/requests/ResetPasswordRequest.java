package com.ga.JNews.models.requests;

import lombok.Getter;

@Getter
public class ResetPasswordRequest {
    private String token;
    private String password;
    private String confirmPassword;
}
