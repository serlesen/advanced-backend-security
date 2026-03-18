package com.sergio.advanced_backend_security.repositories;

import com.sergio.advanced_backend_security.entities.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    Optional<Project> findById(Long id);
    List<Project> findAll();
    Project save(Project project);
    void deleteById(Long id);
    boolean existsById(Long id);
}
