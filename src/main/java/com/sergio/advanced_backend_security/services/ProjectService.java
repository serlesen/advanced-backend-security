package com.sergio.advanced_backend_security.services;

import com.sergio.advanced_backend_security.dtos.ProjectRequestDto;
import com.sergio.advanced_backend_security.dtos.ProjectResponseDto;
import com.sergio.advanced_backend_security.entities.Project;
import com.sergio.advanced_backend_security.repositories.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<ProjectResponseDto> findAll() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponseDto findById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        return toResponse(project);
    }

    public ProjectResponseDto create(ProjectRequestDto dto) {
        Project project = new Project(null, dto.name(), dto.description(), dto.status(), dto.userId());
        return toResponse(projectRepository.save(project));
    }

    public ProjectResponseDto update(Long id, ProjectRequestDto dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        if (dto.name() != null) project.setName(dto.name());
        if (dto.description() != null) project.setDescription(dto.description());
        if (dto.status() != null) project.setStatus(dto.status());
        if (dto.userId() != null) project.setUserId(dto.userId());
        return toResponse(projectRepository.save(project));
    }

    public void delete(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new IllegalArgumentException("Project not found: " + id);
        }
        projectRepository.deleteById(id);
    }

    private ProjectResponseDto toResponse(Project project) {
        return new ProjectResponseDto(project.getId(), project.getName(), project.getDescription(), project.getStatus(), project.getUserId());
    }
}
