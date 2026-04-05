package com.ga.JNews.services;

import com.ga.JNews.exceptions.AccessDeniedException;
import com.ga.JNews.exceptions.AuthenticationException;
import com.ga.JNews.exceptions.BadRequestException;
import com.ga.JNews.exceptions.InformationNotFoundException;
import com.ga.JNews.models.User;
import com.ga.JNews.models.Verification;
import com.ga.JNews.models.enums.TOKEN_TYPE;
import com.ga.JNews.repositories.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VerificationService {
    private final VerificationRepository verificationRepository;
    private final UserService userService;

    @Autowired
    public VerificationService(VerificationRepository verificationRepository, UserService userService) {
        this.verificationRepository = verificationRepository;
        this.userService = userService;
    }

    /**
     * Generate a 128-bit UUID verification token for a user.
     * @return Verification Verification Token
     */
    public Verification generateVerificationToken(User user, TOKEN_TYPE tokenType) {
        if (!userService.findUserByEmail(user.getEmail()).equals(user)) {
            throw new InformationNotFoundException("User with this e-mail does not exist.");
        }

        Verification token = new Verification();
        token.setToken(UUID.randomUUID().toString());
        token.setType(tokenType);
        token.setExpiryDate(expiryTimeByTokenType(tokenType));
        token.setUser(user);
        verificationRepository.save(token);

        return token;
    }

    /**
     * Reissue a new verification token to replace expired token upon request.
     * @param token Verification UUID 128-bit token
     * @return Verification token
     */
    public Verification reissueVerificationToken(String token) {
        Verification verificationToken = verificationRepository.findByToken(token);

        if (!(verificationToken.getToken()).equals(token)) {
            throw new InformationNotFoundException("Verification token does not exist");
        }

        if (!verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token is not expired.");
        }

        User user = userService.findUserByEmail(verificationToken.getUser().getEmail());

        if (user == null) {
            throw new InformationNotFoundException("User with this e-mail does not exist.");
        }

        verificationRepository.delete(verificationToken); // Delete the old token from db.

        Verification newVerificationToken = generateVerificationToken(user, verificationToken.getType());

        return verificationRepository.save(newVerificationToken);
    }

    /**
     * Verify user's e-mail token. Returns true if successful.
     * @param token String Unique token for the user.
     * @return boolean True if successful, otherwise throws a caught error.
     */
    public boolean verifyEmailToken(String token) {
        Verification verificationToken =  verificationRepository.findByToken(token);

        if (!verificationToken.getToken().equals(token)) {
            throw new AuthenticationException("Invalid email verification token.");
        }

        if (!verificationToken.getType().equals(TOKEN_TYPE.EMAIL_VERIFICATION_TOKEN)) {
            throw new AuthenticationException("This is not an email verification token");
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification token is expired. Please request a new one or contact an administrator for support.");
        }

        User user = verificationToken.getUser();

        if (!userService.userExists(user.getEmail())) {
            throw new AuthenticationException("User with this e-mail does not exist.");
        }

        if (user.getIsDeleted()) {
            throw new AccessDeniedException("This user has been deactivated. Please contact an administrator for support.");
        }

        user.setIsVerified(true);
        userService.updateUser(user);
        verificationRepository.delete(verificationToken);

        return true;
    }

    /**
     * Get expiry time by token type. Default value is 15 minutes from now.
     * @param tokenType TOKEN_TYPE
     * @return LocalDateTime
     */
    public LocalDateTime expiryTimeByTokenType(TOKEN_TYPE tokenType) {
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(15); // default value

        switch (tokenType) {
            case EMAIL_VERIFICATION_TOKEN ->   {
                return LocalDateTime.now().plusDays(1);
            }

            case PASSWORD_RESET_TOKEN ->  {
                return LocalDateTime.now().plusMinutes(20);
            }
        }

        return expiryTime;
    }

    /**
     * Verify user's reset password token. Deletes it from database and returns true if successful.
     * @param token String Unique token for the user.
     * @return boolean True if successful, otherwise throws a caught error.
     */
    public boolean verifyResetPasswordToken(String token) {
        Verification verificationToken =  verificationRepository.findByToken(token);

        if (!verificationToken.getToken().equals(token)) {
            throw new AuthenticationException("Invalid password reset token.");
        }

        if (!verificationToken.getType().equals(TOKEN_TYPE.PASSWORD_RESET_TOKEN)) {
            throw new AuthenticationException("This is not a password reset token");
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification token is expired. Please request a new one or contact an administrator for support.");
        }

        User user = verificationToken.getUser();

        if (!userService.userExists(user.getEmail())) {
            throw new AuthenticationException("User with this e-mail does not exist.");
        }

        if (user.getIsDeleted()) {
            throw new AccessDeniedException("This user has been deactivated. Please contact an administrator for support.");
        }

        return true;
    }

    /**
     * Get user from token.
     * @param token String
     * @return User
     */
    public User getUserByToken(String token) {
        Verification verificationToken =  verificationRepository.findByToken(token);

        if (!verificationToken.getToken().equals(token)) {
            throw new AuthenticationException("Invalid token.");
        }

        User user = verificationToken.getUser();

        if (user == null) {
            throw new InformationNotFoundException("This token's user does not exist.");
        }

        return user;
    }

    /**
     * Delete a token from Verification Tokens table.
     * @param verificationToken String token
     */
    public void deleteToken(String verificationToken) {
        Verification token = verificationRepository.findByToken(verificationToken);

        if (token == null) {
            throw new InformationNotFoundException("This verification token does not exist.");
        }

        verificationRepository.delete(token);
    }
}
