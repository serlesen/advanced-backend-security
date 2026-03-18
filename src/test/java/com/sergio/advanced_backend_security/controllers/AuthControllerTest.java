package com.sergio.advanced_backend_security.controllers;

import com.sergio.advanced_backend_security.security.SecurityConfig;
import tools.jackson.databind.ObjectMapper;
import com.sergio.advanced_backend_security.dtos.LoginRequestDto;
import com.sergio.advanced_backend_security.dtos.LoginResponseDto;
import com.sergio.advanced_backend_security.dtos.UserRequestDto;
import com.sergio.advanced_backend_security.dtos.UserResponseDto;
import com.sergio.advanced_backend_security.security.JwtUtil;
import com.sergio.advanced_backend_security.security.UserDetailsServiceImpl;
import com.sergio.advanced_backend_security.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({JacksonAutoConfiguration.class, SecurityConfig.class})
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void login_returnsTokenWith200() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponseDto("jwt-token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto("alice", "pass"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void register_returnsCreatedUserWith201() throws Exception {
        when(authService.register(any())).thenReturn(new UserResponseDto(1L, "alice", "ROLE_USER"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserRequestDto("alice", "pass", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }
}
