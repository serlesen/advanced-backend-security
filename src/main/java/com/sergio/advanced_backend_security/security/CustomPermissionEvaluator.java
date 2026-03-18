package com.sergio.advanced_backend_security.security;

import com.sergio.advanced_backend_security.entities.Project;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if (!(targetDomainObject instanceof Project project)) {
            return false;
        }

        String requiredPermission = (String) permission;
        UserPrincipal user = (UserPrincipal) auth.getPrincipal();

        return switch (requiredPermission) {
            case "WRITE" -> isOwner(project, user) || hasOrganizationAccess(project, user);
            case "READ" -> true;
            default -> false;
        };
    }

    private boolean isOwner(Project project, UserPrincipal user) {
        return project.getOwnerId() != null && project.getOwnerId().equals(user.getId());
    }

    private boolean hasOrganizationAccess(Project project, UserPrincipal user) {
        return user.getOrgId() != null
                && user.getOrgId().equals(project.getOrganizationId())
                && user.getRoles().contains("ROLE_MANAGER");
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}
