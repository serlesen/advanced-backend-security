package com.sergio.advanced_backend_security.services;

import com.sergio.advanced_backend_security.dtos.LoginRequestDto;
import com.sergio.advanced_backend_security.dtos.LoginResponseDto;
import com.sergio.advanced_backend_security.dtos.UserRequestDto;
import com.sergio.advanced_backend_security.dtos.UserResponseDto;
import com.sergio.advanced_backend_security.entities.User;
import com.sergio.advanced_backend_security.repositories.UserRepository;
import com.sergio.advanced_backend_security.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_withValidCredentials_returnsToken() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(jwtUtil.generateToken("alice")).thenReturn("jwt-token");

        LoginResponseDto result = authService.login(new LoginRequestDto("alice", "pass"));

        assertThat(result.token()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken("alice");
    }

    @Test
    void register_withNewUsername_encodesPasswordAndReturnsDto() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded-pass");
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponseDto result = authService.register(new UserRequestDto("alice", "pass", null));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("alice");
        assertThat(result.role()).isEqualTo("ROLE_USER");
        verify(passwordEncoder).encode("pass");
    }

    @Test
    void register_withExistingUsername_throwsException() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(new User(1L, "alice", "encoded", "ROLE_USER")));

        assertThatThrownBy(() -> authService.register(new UserRequestDto("alice", "pass", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("alice");
        verify(userRepository, never()).save(any());
    }
}
