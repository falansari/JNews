package com.ga.JNews.controllers;

import com.ga.JNews.models.Profile;
import com.ga.JNews.services.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "profile")
public class ProfileController {
    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Get user's profile.
     * @return Profile
     */
    @GetMapping("")
    public Profile getProfile() {
        return profileService.getProfile();
    }

    /**
     * Create user profile.
     * @param profile Profile firstName, lastName, photo (uses default placeholder.png), logged-in User
     * @return Profile
     */
    @PostMapping("/create")
    public Profile createProfile(@RequestBody Profile profile) {
        return profileService.createProfile(profile);
    }

    /**
     * Download stored user's CPR image.
     * @return ResponseEntity Resource The image
     */
    @GetMapping("/photo")
    public ResponseEntity<Resource> downloadProfilePhoto() {
        return profileService.downloadPhoto();
    }

    /**
     * Upload user's profile photo
     * @param file MultipartFile [PNG, JPEG]
     * @return ResponseEntity Resource The newly uploaded photo
     */
    @PostMapping(path = "photo/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> uploadProfilePhoto(@RequestParam("file") MultipartFile file) {
        return profileService.uploadPhoto(file);
    }
}
