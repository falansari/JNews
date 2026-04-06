package com.ga.JNews.services;

import com.ga.JNews.exceptions.*;
import com.ga.JNews.models.User;
import com.ga.JNews.models.Verification;
import com.ga.JNews.models.enums.ROLE;
import com.ga.JNews.models.enums.TOKEN_TYPE;
import com.ga.JNews.models.requests.ChangePasswordRequest;
import com.ga.JNews.models.requests.ForgotPasswordRequest;
import com.ga.JNews.models.requests.LoginRequest;
import com.ga.JNews.models.requests.ResetPasswordRequest;
import com.ga.JNews.models.responses.ChangePasswordResponse;
import com.ga.JNews.models.responses.ForgotPasswordResponse;
import com.ga.JNews.models.responses.LoginResponse;
import com.ga.JNews.models.responses.ResetPasswordResponse;
import com.ga.JNews.repositories.UserRepository;
import com.ga.JNews.security.JWTUtils;
import com.ga.JNews.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private MyUserDetails myUserDetails;
    private final AuthenticationManager authenticationManager;
    private final VerificationService verificationService;
    private final MailService mailService;

    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder,
                       JWTUtils jwtUtils,
                       @Lazy AuthenticationManager authenticationManager, // @Lazy will not init this instance unless it's required
                       @Lazy MyUserDetails myUserDetails,
                       @Lazy VerificationService verificationService,
                       @Lazy MailService mailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.myUserDetails = myUserDetails;
        this.verificationService = verificationService;
        this.mailService = mailService;
    }

    /**
     * Create new CAMPAIGN_MANAGER user object with own verification token in database.
     * @param user New user data
     * @return User Saved user object
     */
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            User existingUser = userRepository.findByEmail(user.getEmail());

            if (existingUser.getIsDeleted()) { // re-enable user's account
                existingUser.setIsDeleted(false);
                return userRepository.save(existingUser);
            } else {
                throw new InformationExistException("A user with this email already exists");
            }
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(ROLE.CAMPAIGN_MANAGER);
        user.setIsDeleted(false);
        user.setIsVerified(false);
        User savedUser = userRepository.save(user);

        // Generate & save a verification token for the new user
        Verification token = verificationService.generateVerificationToken(savedUser, TOKEN_TYPE.EMAIL_VERIFICATION_TOKEN);
        mailService.sendVerificationMail(user, token.getToken()); // Send e-mail to user with token

        return savedUser;
    }

    /**
     * Get user object by email address.
     * @param email String
     * @return User
     */
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Check if a user record with the supplied email exists.
     * @param email String User's email address
     * @return boolean True if exists, otherwise false.
     */
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Process user login request.
     * @param loginRequest LoginRequest
     * @return ResponseEntity
     */
    public ResponseEntity<?> loginUser(LoginRequest loginRequest) { // <?> means any type
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        try {
            Authentication authentication = authenticationManager
                    .authenticate(authenticationToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            myUserDetails = (MyUserDetails) authentication.getPrincipal();
            Assert.notNull(myUserDetails, "Authentication principal is null");
            final String JWT = jwtUtils.generateJwtToken(myUserDetails);
            return ResponseEntity.ok(new LoginResponse(JWT));
        } catch (AuthenticationException e) {
            return ResponseEntity.ok(new LoginResponse("Error: Invalid username or password"));
        } catch (DisabledException e) {
            return ResponseEntity.ok(new LoginResponse("Error: This user is disabled. Please contact an admin for support."));
        } catch (BadCredentialException e) {
            return ResponseEntity.ok(new LoginResponse("Error: User with this e-mail does not exist"));
        }
    }

    /**
     * Update user's saved record in database.
     * @param user User user object, must have a valid e-mail address.
     * @return User updated user.
     */
    public User updateUser(User user) {
        if (!userRepository.existsByEmail(user.getEmail())) {
            throw new InformationNotFoundException("User with e-mail " + user.getEmail() + " does not exist.");
        }

        return userRepository.save(user);
    }

    /**
     * Get current logged in user.
     * @return User
     */
    public static User getCurrentLoggedInUser() {
        MyUserDetails userDetails = (MyUserDetails) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Assert.notNull(userDetails, "Current logged in user is null");
        return userDetails.getUser();
    }

    /**
     * Change logged-in user's password.
     * @param changePasswordRequest ChangePasswordRequest oldPassword, newPassword, confirmNewPassword
     * @return ChangePasswordResponse OK / error message
     */
    public ChangePasswordResponse changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = getCurrentLoggedInUser();

        // RULE 1: User inputs correct old password
        boolean passwordMatches = passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword());
        if (!passwordMatches) {
            throw new BadCredentialException("Invalid old password");
        }

        // RULE 2: User inputs new password
        Assert.notNull(changePasswordRequest.getNewPassword(), "New password is null");

        // RULE 3: User confirms new password entry
        boolean newPasswordConfirmed = Objects.equals(changePasswordRequest.getNewPassword(), changePasswordRequest.getConfirmNewPassword());
        if (!newPasswordConfirmed) {
            throw new BadCredentialException("New password and confirm new password are not a match");
        }

        // RULE 4: New password must not be the same as the old password
        boolean passwordSame = passwordEncoder.matches(changePasswordRequest.getNewPassword(), user.getPassword());
        if (passwordSame) {
            throw new BadCredentialException("New password must not match old password");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);

        return new ChangePasswordResponse("Password changed successfully");
    }

    /**
     * Validate forget password request and send reset password token via e-mail to the user.
     * @param forgotPasswordRequest ForgotPasswordRequest email
     * @return ForgotPasswordResponse OK message
     */
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        User user = userRepository.findByEmail(forgotPasswordRequest.getEmail());
        String response = "An e-mail with a reset password token has been sent if this e-mail exists. Please use the token to reset your password";

        if (user == null) { // User with this e-mail does not exist. Do nothing.
            return new ForgotPasswordResponse(response);
        }

        // Generate & mail  a password reset token for the user
        Verification token = verificationService.generateVerificationToken(user, TOKEN_TYPE.PASSWORD_RESET_TOKEN);
        mailService.sendPasswordResetMail(user, token.getToken());

        return new ForgotPasswordResponse(response);
    }

    /**
     * Reset user's password.
     * @param resetPasswordRequest ResetPasswordRequest token, password, confirmPassword
     * @return ResetPasswordResponse OK / error message
     */
    public ResetPasswordResponse resetPassword(ResetPasswordRequest resetPasswordRequest) {
        // RULE 1: User inputs correct token
        boolean tokenIsValid = verificationService.verifyResetPasswordToken(resetPasswordRequest.getToken());
        if (!tokenIsValid) {
            throw new BadCredentialException("Invalid reset password token");
        }

        // RULE 2: User inputs new password
        Assert.notNull(resetPasswordRequest.getPassword(), "New password is null");

        // RULE 3: User confirms new password entry
        boolean newPasswordConfirmed = Objects.equals(resetPasswordRequest.getPassword(), resetPasswordRequest.getConfirmPassword());
        if (!newPasswordConfirmed) {
            throw new BadCredentialException("New password and confirm new password are not a match");
        }

        User user = verificationService.getUserByToken(resetPasswordRequest.getToken());

        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getPassword()));
        userRepository.save(user);
        verificationService.deleteToken(resetPasswordRequest.getToken());

        return new ResetPasswordResponse("Password reset successfully. Please login again.");
    }

    /**
     * Set user's status to deleted.
     * @param userId Long User's ID
     */
    public void softDeleteUser(Long userId) {
        if (getCurrentLoggedInUser().getRole() != ROLE.ADMIN) {
            throw new AccessDeniedException("You do not have permission to perform this action.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new InformationNotFoundException("User with id " + userId + " does not exist"));

        user.setIsDeleted(true);
        userRepository.save(user);
    }

    /**
     * Create the default admin user in database for new system setup. An admin must not already exist.
     * @param admin User email, password
     * @return User
     */
    public User createDefaultAdminUser(User admin) {
        if (userRepository.existsByRole(ROLE.ADMIN)) {
            throw new InformationExistException("User with role " + ROLE.ADMIN + " already exists.");
        }

        User createdAdmin = createUser(admin);
        createdAdmin.setRole(ROLE.ADMIN);

        return updateUser(createdAdmin);
    }

    /**
     * Create ADMIN user. Only admins may create other admin user accounts.
     * @param user User
     * @return User
     */
    public User createAdminUser(User user) {
        if (getCurrentLoggedInUser().getRole() != ROLE.ADMIN) {
            throw new AccessDeniedException("You do not have permission to perform this action.");
        }

        User admin = createUser(user);
        admin.setRole(ROLE.ADMIN);

        return updateUser(admin);
    }
}