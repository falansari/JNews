package com.ga.JNews.controllers;

import com.ga.JNews.models.Verification;
import com.ga.JNews.services.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class VerificationController {

    @Autowired
    private VerificationService verificationService;

    @PostMapping("/users/verify")
    public ResponseEntity<String> verify(@RequestParam("token") String token) {
        boolean isVerified = verificationService.verifyEmailToken(token);

        if (isVerified) {
            return ResponseEntity.ok("Account is verified. You can now login.");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }
    }

    @PostMapping("/users/token")
    public Verification reissueToken(@RequestParam String token) {
        return verificationService.reissueVerificationToken(token);
    }
}
