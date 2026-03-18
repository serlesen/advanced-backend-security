package com.sergio.advanced_backend_security.controllers;

import com.sergio.advanced_backend_security.dtos.ProjectRequestDto;
import com.sergio.advanced_backend_security.dtos.ProjectResponseDto;
import com.sergio.advanced_backend_security.services.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponseDto> findAll() {
        return projectService.findAll();
    }

    @GetMapping("/{id}")
    public ProjectResponseDto findById(@PathVariable Long id) {
        return projectService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponseDto create(@RequestBody ProjectRequestDto dto) {
        return projectService.create(dto);
    }

    @PutMapping("/{id}")
    public ProjectResponseDto update(@PathVariable Long id, @RequestBody ProjectRequestDto dto) {
        return projectService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        projectService.delete(id);
    }
}
