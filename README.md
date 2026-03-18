# Advanced Backend Security

> Project by **Sergio Lema** — [YouTube: The Dev World](https://www.youtube.com/@thedevworld) · [Blog: sergiolema.dev](https://sergiolema.dev)

Spring Boot 4 REST API with JWT authentication and in-memory persistence.

## Project Structure

```
src/main/java/com/sergio/advanced_backend_security/
├── controllers/
│   ├── AuthController.java          # POST /auth/register, POST /auth/login
│   ├── UserController.java          # CRUD /users
│   └── ProjectController.java       # CRUD /projects
├── dtos/
│   ├── LoginRequestDto.java
│   ├── LoginResponseDto.java
│   ├── UserRequestDto.java
│   ├── UserResponseDto.java
│   ├── ProjectRequestDto.java
│   └── ProjectResponseDto.java
├── entities/
│   ├── User.java                    # id, username, password, role, organizationId
│   └── Project.java                 # id, name, description, status, ownerId, organizationId
├── repositories/
│   ├── UserRepository.java          # interface
│   ├── InMemoryUserRepository.java
│   ├── ProjectRepository.java       # interface
│   └── InMemoryProjectRepository.java
├── security/
│   ├── JwtUtil.java                 # token generation and validation
│   ├── JwtAuthenticationFilter.java
│   ├── UserDetailsServiceImpl.java
│   ├── UserPrincipal.java           # custom UserDetails exposing id and organizationId
│   ├── CustomPermissionEvaluator.java  # domain-object permission logic
│   └── SecurityConfig.java
└── services/
    ├── AuthService.java
    ├── UserService.java
    └── ProjectService.java
```

## Requirements

- Java 21
- Maven (wrapper included)

## Running the Application

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

## API Endpoints

### Auth (public)

| Method | Endpoint         | Body                                                                   |
|--------|------------------|------------------------------------------------------------------------|
| POST   | /auth/register   | `{"username":"","password":"","role":"","organizationId":1}`           |
| POST   | /auth/login      | `{"username":"","password":""}`                                        |

Login returns a JWT token:
```json
{ "token": "<jwt>" }
```

### Users (requires JWT)

| Method | Endpoint     | Body                                                                          |
|--------|--------------|-------------------------------------------------------------------------------|
| GET    | /users       |                                                                               |
| GET    | /users/{id}  |                                                                               |
| POST   | /users       | `{"username":"","password":"","role":"","organizationId":1}`                  |
| PUT    | /users/{id}  | `{"username":"","password":"","role":"","organizationId":1}`                  |
| DELETE | /users/{id}  |                                                                               |

### Projects (requires JWT)

| Method | Endpoint        | Body                                                                              |
|--------|-----------------|-----------------------------------------------------------------------------------|
| GET    | /projects       | — requires `ROLE_ADMIN` or `ROLE_MANAGER`                                         |
| GET    | /projects/{id}  |                                                                                   |
| POST   | /projects       | `{"name":"","description":"","status":"","ownerId":1,"organizationId":1}`         |
| PUT    | /projects/{id}  | `{"name":"","description":"","status":"","ownerId":1,"organizationId":1}`         |
| DELETE | /projects/{id}  | — requires `ROLE_ADMIN`                                                           |

Pass the token as a Bearer header on all protected requests:
```
Authorization: Bearer <token>
```

## Example Usage

```bash
# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"pass123","role":"ROLE_USER"}'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"pass123"}'

# Create a project
curl -X POST http://localhost:8080/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"My App","description":"A cool project","status":"active","ownerId":1,"organizationId":10}'

# List projects
curl http://localhost:8080/projects \
  -H "Authorization: Bearer <token>"
```

## Granular Authorization

This project implements three layers of method-level security on top of JWT authentication.

### Step 1 — Role Hierarchy

Defined in `SecurityConfig`, the hierarchy `ADMIN > MANAGER > USER` means a higher role automatically inherits all permissions of the roles below it. A user with `ROLE_ADMIN` passes any check that requires `ROLE_MANAGER` or `ROLE_USER` without extra configuration.

```
ROLE_ADMIN > ROLE_MANAGER > ROLE_USER
```

`@EnableMethodSecurity` activates `@PreAuthorize` / `@PostAuthorize` processing on Spring beans. The `MethodSecurityExpressionHandler` bean wires the hierarchy and the custom permission evaluator into every SpEL expression.

### Step 2 — Attribute-Based Access Control via SpEL

Spring Expression Language lets you write inline policy directly on a method. The expression has access to the method's parameters (`#project`) and the security context (`authentication.principal`).

```java
@PreAuthorize("hasRole('ADMIN') or #project.ownerId == authentication.principal.id")
public ProjectResponseDto updateProject(Project project) { ... }
```

`authentication.principal` resolves to `UserPrincipal`, a custom `UserDetails` implementation that exposes `getId()` and `getOrgId()` so they are usable in SpEL expressions. Without this, `principal` would be a plain Spring `User` object with no domain fields.

### Step 3 — Custom `PermissionEvaluator`

When the logic is too complex for a one-liner (database lookups, multi-condition rules), move it to `CustomPermissionEvaluator`. It handles the question *"can this principal perform action X on domain object Y?"* in one place, keeping service methods clean.

```java
@PreAuthorize("hasPermission(#project, 'WRITE')")
public ProjectResponseDto secureUpdate(Project project) { ... }
```

`CustomPermissionEvaluator.hasPermission` resolves `WRITE` access as:

| Condition | Granted |
|-----------|---------|
| Caller is the project owner (`ownerId == user.id`) | yes |
| Caller shares the organization (`orgId == project.organizationId`) **and** has `ROLE_MANAGER` | yes |
| Anything else | no |

`READ` is granted to all authenticated callers. Unknown permissions are denied by default.

### Key classes

| Class | Role |
|-------|------|
| `UserPrincipal` | Wraps the `User` entity; exposes `id` and `orgId` to SpEL |
| `CustomPermissionEvaluator` | Centralized domain-object permission logic |
| `SecurityConfig` | Declares role hierarchy and wires everything into the expression handler |

## Configuration

JWT settings are in `src/main/resources/application.properties`:

```properties
jwt.secret=advanced-backend-security-super-secret-key-minimum-256-bits-long
jwt.expiration=86400000   # 24 hours in milliseconds
```

## Author

**Sergio Lema**

- YouTube: [The Dev World](https://www.youtube.com/@TheDevWorldbySergioLema)
- Blog: [sergiolema.dev](https://sergiolema.dev)
