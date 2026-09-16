package com.transaction.management.controller;

import com.transaction.management.dto.LoginRequest;
import com.transaction.management.dto.RegistrationRequest;
import com.transaction.management.entity.User;
import com.transaction.management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public UserController(
            UserService userService,
            AuthenticationManager authenticationManager) {

        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestBody RegistrationRequest request) {

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setPassword(request.getPassword());
        user.setOpeningBalance(request.getOpeningBalance());

        userService.registerUser(user);

        return "Registration successful";
    }

    @PostMapping("/login")
    public String loginUser(
            @RequestBody LoginRequest request,
            HttpSession session) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        userService.loginUser(
                request.getEmail(),
                request.getPassword()
        );

        session.setAttribute(
                "SPRING_SECURITY_CONTEXT",
                new org.springframework.security.core.context.SecurityContextImpl(
                        authentication
                )
        );

        return "Login successful";
    }

    @PostMapping("/logout")
    public String logoutUser(
            HttpSession session) {

        session.invalidate();

        return "Logout successful";
    }
    @GetMapping("/me")
public User getCurrentUser(
        org.springframework.security.core.Authentication authentication) {

    return userService.getUserByEmail(authentication.getName());
}
}