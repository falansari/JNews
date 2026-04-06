package com.ga.JNews.controllers;

import com.ga.JNews.models.User;
import com.ga.JNews.models.requests.ChangePasswordRequest;
import com.ga.JNews.models.requests.ForgotPasswordRequest;
import com.ga.JNews.models.requests.LoginRequest;
import com.ga.JNews.models.requests.ResetPasswordRequest;
import com.ga.JNews.models.responses.ChangePasswordResponse;
import com.ga.JNews.models.responses.ForgotPasswordResponse;
import com.ga.JNews.models.responses.ResetPasswordResponse;
import com.ga.JNews.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/auth/users")
public class UserController {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return userService.loginUser(loginRequest);
    }

    /**
     * Soft delete user by user's ID.
     * @param userId Long
     */
    @DeleteMapping("/{userId}")
    public void softDeleteUser(@PathVariable Long userId) {
        userService.softDeleteUser(userId);
    }

    /**
     * Change logged-in user's password with validation rules.
     * @param changePasswordRequest ChangePasswordRequest oldPassword, newPassword, confirmNewPassword
     * @return ChangePasswordResponse OK / error message.
     */
    @PostMapping("/change-password")
    public ChangePasswordResponse changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        return userService.changePassword(changePasswordRequest);
    }

    /**
     * Send reset password token to verified user's e-mail address.
     * @param forgotPasswordRequest ForgotPasswordRequest email
     * @return ForgotPasswordResponse OK message
     */
    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        return userService.forgotPassword(forgotPasswordRequest);
    }

    @PostMapping("/reset")
    public ResetPasswordResponse resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        return userService.resetPassword(resetPasswordRequest);
    }
}
