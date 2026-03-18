package com.sergio.advanced_backend_security.controllers;

import tools.jackson.databind.ObjectMapper;
import com.sergio.advanced_backend_security.dtos.ProjectRequestDto;
import com.sergio.advanced_backend_security.dtos.ProjectResponseDto;
import com.sergio.advanced_backend_security.security.CustomPermissionEvaluator;
import com.sergio.advanced_backend_security.security.JwtUtil;
import com.sergio.advanced_backend_security.security.UserDetailsServiceImpl;
import com.sergio.advanced_backend_security.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.sergio.advanced_backend_security.security.SecurityConfig;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({JacksonAutoConfiguration.class, SecurityConfig.class})
@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private CustomPermissionEvaluator customPermissionEvaluator;

    @Test
    @WithMockUser
    void findAll_authenticated_returns200WithList() throws Exception {
        when(projectService.findAll()).thenReturn(List.of(
                new ProjectResponseDto(1L, "Website Redesign", "Redesign the company website with modern UI", "active", 1L),
                new ProjectResponseDto(2L, "Mobile App MVP", "First version of the iOS and Android mobile application", "draft", 1L)
        ));

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Website Redesign"))
                .andExpect(jsonPath("$[1].name").value("Mobile App MVP"));
    }

    @Test
    void findAll_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void findById_authenticated_returns200WithProject() throws Exception {
        when(projectService.findById(1L)).thenReturn(new ProjectResponseDto(1L, "Website Redesign", "Redesign the company website with modern UI", "active", 1L));

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Website Redesign"))
                .andExpect(jsonPath("$.status").value("active"));
    }

    @Test
    @WithMockUser
    void create_authenticated_returns201WithProject() throws Exception {
        when(projectService.create(any())).thenReturn(new ProjectResponseDto(1L, "Website Redesign", "Redesign the company website with modern UI", "active", 1L));

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProjectRequestDto("Website Redesign", "Redesign the company website with modern UI", "active", 1L, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Website Redesign"));
    }

    @Test
    @WithMockUser
    void update_authenticated_returns200WithUpdatedProject() throws Exception {
        when(projectService.update(eq(1L), any())).thenReturn(new ProjectResponseDto(1L, "Website Redesign v2", "Updated project scope and deliverables", "active", 1L));

        mockMvc.perform(put("/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProjectRequestDto("Website Redesign v2", "Updated project scope and deliverables", "active", 1L, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Website Redesign v2"))
                .andExpect(jsonPath("$.status").value("active"));
    }

    @Test
    @WithMockUser
    void delete_authenticated_returns204() throws Exception {
        doNothing().when(projectService).delete(1L);

        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isNoContent());
    }
}
