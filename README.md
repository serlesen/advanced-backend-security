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
│   ├── User.java                    # id, username, password, role
│   └── Project.java                 # id, name, description, price, userId
├── repositories/
│   ├── UserRepository.java          # interface
│   ├── InMemoryUserRepository.java
│   ├── ProjectRepository.java       # interface
│   └── InMemoryProjectRepository.java
├── security/
│   ├── JwtUtil.java                 # token generation and validation
│   ├── JwtAuthenticationFilter.java
│   ├── UserDetailsServiceImpl.java
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

| Method | Endpoint         | Body                                      |
|--------|------------------|-------------------------------------------|
| POST   | /auth/register   | `{"username":"","password":"","role":""}` |
| POST   | /auth/login      | `{"username":"","password":""}`           |

Login returns a JWT token:
```json
{ "token": "<jwt>" }
```

### Users (requires JWT)

| Method | Endpoint     | Body                                      |
|--------|--------------|-------------------------------------------|
| GET    | /users       |                                           |
| GET    | /users/{id}  |                                           |
| POST   | /users       | `{"username":"","password":"","role":""}` |
| PUT    | /users/{id}  | `{"username":"","password":"","role":""}` |
| DELETE | /users/{id}  |                                           |

### Projects (requires JWT)

| Method | Endpoint        | Body                                                                    |
|--------|-----------------|-------------------------------------------------------------------------|
| GET    | /projects       |                                                                         |
| GET    | /projects/{id}  |                                                                         |
| POST   | /projects       | `{"name":"","description":"","price":0.0,"userId":1}`                   |
| PUT    | /projects/{id}  | `{"name":"","description":"","price":0.0,"userId":1}`                   |
| DELETE | /projects/{id}  |                                                                         |

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
  -d '{"name":"My App","description":"A cool project","price":999.99,"userId":1}'

# List projects
curl http://localhost:8080/projects \
  -H "Authorization: Bearer <token>"
```

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
