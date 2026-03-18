package com.sergio.advanced_backend_security.repositories;

import com.sergio.advanced_backend_security.entities.Project;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryProjectRepository implements ProjectRepository {

    private final Map<Long, Project> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<Project> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Project> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Project save(Project project) {
        if (project.getId() == null) {
            project.setId(idGenerator.getAndIncrement());
        }
        store.put(project.getId(), project);
        return project;
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }
}
