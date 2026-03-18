package com.sergio.advanced_backend_security.services;

import com.sergio.advanced_backend_security.dtos.LoginRequestDto;
import com.sergio.advanced_backend_security.dtos.LoginResponseDto;
import com.sergio.advanced_backend_security.dtos.UserRequestDto;
import com.sergio.advanced_backend_security.dtos.UserResponseDto;
import com.sergio.advanced_backend_security.entities.User;
import com.sergio.advanced_backend_security.repositories.UserRepository;
import com.sergio.advanced_backend_security.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public LoginResponseDto login(LoginRequestDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
        );
        return new LoginResponseDto(jwtUtil.generateToken(dto.username()));
    }

    public UserResponseDto register(UserRequestDto dto) {
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            throw new IllegalArgumentException("Username already taken: " + dto.username());
        }
        User user = new User(null, dto.username(), passwordEncoder.encode(dto.password()), "ROLE_USER");
        User saved = userRepository.save(user);
        return new UserResponseDto(saved.getId(), saved.getUsername(), saved.getRole());
    }
}
