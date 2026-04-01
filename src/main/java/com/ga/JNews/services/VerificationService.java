package com.ga.JNews.services;

import com.ga.JNews.exceptions.AccessDeniedException;
import com.ga.JNews.exceptions.AuthenticationException;
import com.ga.JNews.exceptions.BadRequestException;
import com.ga.JNews.exceptions.InformationNotFoundException;
import com.ga.JNews.models.User;
import com.ga.JNews.models.Verification;
import com.ga.JNews.repositories.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VerificationService {
    private final VerificationRepository verificationRepository;
    private final UserService userService;
    private final MailService mailService;

    @Autowired
    public VerificationService(VerificationRepository verificationRepository, UserService userService, MailService mailService) {
        this.verificationRepository = verificationRepository;
        this.userService = userService;
        this.mailService = mailService;
    }

    /**
     * Generate a 128-bit UUID verification token for a new user.
     * @return Verification Verification Token
     */
    public Verification generateVerificationToken(User user) {
        if (!userService.findUserByEmail(user.getEmail()).equals(user)) {
            throw new InformationNotFoundException("User with this e-mail does not exist.");
        }

        Verification token = new Verification();
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusMinutes(20));
        token.setUser(user);
        verificationRepository.save(token);

        mailService.sendVerificationMail(user, token.getToken());

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

        Verification newVerificationToken = generateVerificationToken(user);

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

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification token is expired. Please request a new one or contact an administrator for support.");
        }

        User user = verificationToken.getUser();
        System.out.println("Token: " + verificationToken.getToken());
        System.out.println("User: " + user.getEmail());

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
}
