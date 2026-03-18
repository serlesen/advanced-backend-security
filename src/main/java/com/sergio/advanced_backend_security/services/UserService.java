package com.sergio.advanced_backend_security.services;

import com.sergio.advanced_backend_security.dtos.UserRequestDto;
import com.sergio.advanced_backend_security.dtos.UserResponseDto;
import com.sergio.advanced_backend_security.entities.User;
import com.sergio.advanced_backend_security.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponseDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return toResponse(user);
    }

    public UserResponseDto create(UserRequestDto dto) {
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            throw new IllegalArgumentException("Username already taken: " + dto.username());
        }
        String role = dto.role() != null ? dto.role() : "ROLE_USER";
        User user = new User(null, dto.username(), passwordEncoder.encode(dto.password()), role, dto.organizationId());
        return toResponse(userRepository.save(user));
    }

    public UserResponseDto update(Long id, UserRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        if (dto.username() != null) user.setUsername(dto.username());
        if (dto.password() != null) user.setPassword(passwordEncoder.encode(dto.password()));
        if (dto.role() != null) user.setRole(dto.role());
        return toResponse(userRepository.save(user));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponseDto toResponse(User user) {
        return new UserResponseDto(user.getId(), user.getUsername(), user.getRole());
    }
}
