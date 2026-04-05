package com.ga.JNews.controllers;

import com.ga.JNews.models.User;
import com.ga.JNews.models.requests.ChangePasswordRequest;
import com.ga.JNews.models.requests.LoginRequest;
import com.ga.JNews.models.responses.ChangePasswordResponse;
import com.ga.JNews.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * Change logged-in user's password with validation rules.
     * @param changePasswordRequest ChangePasswordRequest oldPassword, newPassword, confirmNewPassword
     * @return ChangePasswordResponse OK / error message.
     */
    @PostMapping("/change-password")
    public ChangePasswordResponse changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        return userService.changePassword(changePasswordRequest);
    }
}
