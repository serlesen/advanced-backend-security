package com.sergio.advanced_backend_security.services;

import com.sergio.advanced_backend_security.dtos.ProjectRequestDto;
import com.sergio.advanced_backend_security.dtos.ProjectResponseDto;
import com.sergio.advanced_backend_security.entities.Project;
import com.sergio.advanced_backend_security.repositories.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void findAll_returnsAllProjectsAsDtos() {
        when(projectRepository.findAll()).thenReturn(List.of(
                new Project(1L, "Website Redesign", "Redesign the company website with modern UI", "active", 1L, null),
                new Project(2L, "Mobile App MVP", "First version of the iOS and Android mobile application", "draft", 1L, null)
        ));

        List<ProjectResponseDto> result = projectService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Website Redesign");
        assertThat(result.get(1).name()).isEqualTo("Mobile App MVP");
    }

    @Test
    void findById_whenExists_returnsDto() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(new Project(1L, "Website Redesign", "Redesign the company website with modern UI", "active", 1L, null)));

        ProjectResponseDto result = projectService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Website Redesign");
        assertThat(result.description()).isEqualTo("Redesign the company website with modern UI");
        assertThat(result.status()).isEqualTo("active");
        assertThat(result.ownerId()).isEqualTo(1L);
    }

    @Test
    void findById_whenNotFound_throwsException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_savesAndReturnsDto() {
        when(projectRepository.save(any())).thenAnswer(inv -> {
            Project project = inv.getArgument(0);
            project.setId(1L);
            return project;
        });

        ProjectResponseDto result = projectService.create(new ProjectRequestDto("Website Redesign", "Redesign the company website with modern UI", "active", 1L, null));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Website Redesign");
        assertThat(result.status()).isEqualTo("active");
        assertThat(result.ownerId()).isEqualTo(1L);
        verify(projectRepository).save(any());
    }

    @Test
    void update_whenExists_updatesFields() {
        Project existing = new Project(1L, "Website Redesign", "Initial project description", "draft", 1L, null);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.save(existing)).thenReturn(existing);

        ProjectResponseDto result = projectService.update(1L, new ProjectRequestDto("Website Redesign v2", "Updated project scope and deliverables", "active", 1L, null));

        assertThat(result.name()).isEqualTo("Website Redesign v2");
        assertThat(result.description()).isEqualTo("Updated project scope and deliverables");
        assertThat(result.status()).isEqualTo("active");
    }

    @Test
    void update_withNullFields_keepsExistingValues() {
        Project existing = new Project(1L, "Website Redesign", "Redesign the company website with modern UI", "active", 1L, null);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.save(existing)).thenReturn(existing);

        ProjectResponseDto result = projectService.update(1L, new ProjectRequestDto(null, null, null, null, null));

        assertThat(result.name()).isEqualTo("Website Redesign");
        assertThat(result.description()).isEqualTo("Redesign the company website with modern UI");
        assertThat(result.status()).isEqualTo("active");
    }

    @Test
    void update_whenNotFound_throwsException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.update(99L, new ProjectRequestDto("Website Redesign", "Some description", "active", 1L, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_whenExists_callsDeleteById() {
        when(projectRepository.existsById(1L)).thenReturn(true);

        projectService.delete(1L);

        verify(projectRepository).deleteById(1L);
    }

    @Test
    void delete_whenNotFound_throwsException() {
        when(projectRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> projectService.delete(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
        verify(projectRepository, never()).deleteById(any());
    }
}
