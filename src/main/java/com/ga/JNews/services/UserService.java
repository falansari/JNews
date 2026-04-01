package com.ga.JNews.services;

import com.ga.JNews.exceptions.InformationExistException;
import com.ga.JNews.models.User;
import com.ga.JNews.models.requests.LoginRequest;
import com.ga.JNews.models.responses.LoginResponse;
import com.ga.JNews.repositories.UserRepository;
import com.ga.JNews.security.JWTUtils;
import com.ga.JNews.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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

    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder,
                       JWTUtils jwtUtils,
                       @Lazy AuthenticationManager authenticationManager, // @Lazy will not init this instance unless it's required
                       @Lazy MyUserDetails myUserDetails) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.myUserDetails = myUserDetails;
    }

    public User createUser(User userObject) {
        if (!userRepository.existsByEmail(userObject.getEmail())) {
            userObject.setPassword(passwordEncoder.encode(userObject.getPassword()));

            return userRepository.save(userObject);
        } else {
            throw new InformationExistException("A user with the e-mail " + userObject.getEmail() + " already exists.");
        }
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
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
        } catch (Exception e) {
            return ResponseEntity.ok(new LoginResponse("Error: Invalid username or password"));
        }
    }
}