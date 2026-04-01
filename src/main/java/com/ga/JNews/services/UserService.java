package com.ga.JNews.services;

import com.ga.JNews.exceptions.AuthenticationException;
import com.ga.JNews.exceptions.InformationExistException;
import com.ga.JNews.exceptions.InformationNotFoundException;
import com.ga.JNews.models.User;
import com.ga.JNews.models.Verification;
import com.ga.JNews.models.requests.LoginRequest;
import com.ga.JNews.models.responses.LoginResponse;
import com.ga.JNews.repositories.UserRepository;
import com.ga.JNews.security.JWTUtils;
import com.ga.JNews.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private MyUserDetails myUserDetails;
    private final AuthenticationManager authenticationManager;
    private final VerificationService verificationService;

    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder,
                       JWTUtils jwtUtils,
                       @Lazy AuthenticationManager authenticationManager, // @Lazy will not init this instance unless it's required
                       @Lazy MyUserDetails myUserDetails,
                       @Lazy VerificationService verificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.myUserDetails = myUserDetails;
        this.verificationService = verificationService;
    }

    /**
     * Create new user object with own verification token in database.
     * @param userObject New user data
     * @return User Saved user object
     */
    public User createUser(User userObject) {
        if (userRepository.existsByEmail(userObject.getEmail())) {
            throw new InformationExistException("A user with this email already exists");
        }

        userObject.setPassword(passwordEncoder.encode(userObject.getPassword()));
        User savedUser = userRepository.save(userObject);

        // Generate & save a verification token for the new user
        Verification token = verificationService.generateVerificationToken(savedUser);
        System.out.println("Verification Token: " + token.getToken());

        return savedUser;
    }

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

    public ResponseEntity<?> loginUser(LoginRequest loginRequest) { // <?> means any type
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        try {
            Authentication authentication = authenticationManager
                    .authenticate(authenticationToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            myUserDetails = (MyUserDetails) authentication.getPrincipal();
            assert myUserDetails != null;
            final String JWT = jwtUtils.generateJwtToken(myUserDetails);
            return ResponseEntity.ok(new LoginResponse(JWT));
        } catch (AuthenticationException e) {
            return ResponseEntity.ok(new LoginResponse("Error: Invalid username or password"));
        } catch (DisabledException e) {
            return ResponseEntity.ok(new LoginResponse("Error: User unauthorized. Please verify your e-mail before login, else contact an administrator for support."));
        } catch (BadCredentialsException e) {
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
}