package com.ga.JNews.services;

import com.ga.JNews.exceptions.InformationExistException;
import com.ga.JNews.exceptions.InformationNotFoundException;
import com.ga.JNews.models.Profile;
import com.ga.JNews.models.User;
import com.ga.JNews.repositories.ProfileRepository;
import com.ga.JNews.utilities.Uploads;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    final String uploadImagePath = "uploads/profile";
    private final Uploads uploads;
    private final UserService userService;

    @Autowired
    public ProfileService(ProfileRepository profileRepository, Uploads uploads, UserService userService) {
        this.profileRepository = profileRepository;
        this.uploads = uploads;
        this.userService = userService;
    }

    /**
     * Get user's profile.
     * @return Profile
     */
    public Profile getProfile() {
        Profile userProfile = profileRepository.findByUserId(UserService.getCurrentLoggedInUser().getId());

        if (userProfile == null) {
            throw new InformationNotFoundException("A profile for this user was not found");
        }

        return userProfile;
    }

    /**
     * Create profile for new user.
     * @param profile Profile firstName, lastName, photo (uses default placeholder.png), logged-in User
     * @return Profile
     */
    public Profile createProfile(Profile profile) {
        User user = UserService.getCurrentLoggedInUser();

        if (user == null) {
            throw new InformationNotFoundException("User cannot be null");
        }

        boolean profileExists = profileRepository.existsByUser_Id(user.getId());
        if (profileExists) {
            throw new InformationExistException("A profile for this user already exists");
        }

        String firstName = profile.getFirstName();
        String lastName = profile.getLastName();
        String photo = Objects.requireNonNull(uploads.downloadImage(uploadImagePath, "placeholder.png").getBody()).getFilename();

        if (firstName == null || lastName == null) {
            throw new InformationNotFoundException("First name, last name and photo cannot be null");
        }

        // Create profile
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setPhoto(photo);
        profile.setUser(user);

        // Add it to user
        user.setProfile(profile);
        userService.updateUser(user);

        return getProfile();
    }

    /**
     * Download stored user's profile image
     * @return ResponseEntity Resource The stored image if any [PNG, JPEG]
     */
    public ResponseEntity<Resource> downloadPhoto() {
        User user = UserService.getCurrentLoggedInUser();

        if (user == null) throw new InformationNotFoundException("You must login to download profile photo");

        return uploads.downloadImage(uploadImagePath, getProfile().getPhoto());
    }

    /**
     * Upload user profile photo
     * @param file MultipartFile PNG, JPG
     * @return ResponseEntity Resource
     */
    public ResponseEntity<Resource> uploadPhoto(MultipartFile file) {
        Profile profile = getProfile();

        if (profile.getPhoto() != null && !profile.getPhoto().equals("placeholder.png")) uploads.deleteImage(uploadImagePath, profile.getPhoto()); // Delete existing photo from storage

        String newPhoto = uploads.uploadImage(uploadImagePath, file);
        profile.setPhoto(newPhoto);
        profileRepository.save(profile);

        return uploads.downloadImage(uploadImagePath, profile.getPhoto());
    }

    /**
     * Delete user profile's photo and reset it back to placeholder.png default photo.
     * @return ResponseEntity Resource the newly set default profile image
     */
    public ResponseEntity<Resource> deletePhoto() {
        Profile profile = getProfile();

        if (profile.getPhoto() == null || profile.getPhoto().equals("placeholder.png")) {
            return uploads.downloadImage(uploadImagePath, getProfile().getPhoto());
        }

        String defaultPhoto = Objects.requireNonNull(uploads.downloadImage(uploadImagePath, "placeholder.png").getBody()).getFilename();

        uploads.deleteImage(uploadImagePath, profile.getPhoto());
        profile.setPhoto(defaultPhoto);
        profileRepository.save(profile);

        return uploads.downloadImage(uploadImagePath, profile.getPhoto());
    }
}
