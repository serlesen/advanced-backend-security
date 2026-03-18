package com.sergio.advanced_backend_security.services;

import com.sergio.advanced_backend_security.dtos.UserRequestDto;
import com.sergio.advanced_backend_security.dtos.UserResponseDto;
import com.sergio.advanced_backend_security.entities.User;
import com.sergio.advanced_backend_security.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void findAll_returnsAllUsersAsDtos() {
        when(userRepository.findAll()).thenReturn(List.of(
                new User(1L, "alice", "encoded", "ROLE_USER"),
                new User(2L, "bob", "encoded", "ROLE_ADMIN")
        ));

        List<UserResponseDto> result = userService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).username()).isEqualTo("alice");
        assertThat(result.get(1).username()).isEqualTo("bob");
    }

    @Test
    void findById_whenExists_returnsDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User(1L, "alice", "encoded", "ROLE_USER")));

        UserResponseDto result = userService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("alice");
        assertThat(result.role()).isEqualTo("ROLE_USER");
    }

    @Test
    void findById_whenNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_withAvailableUsername_encodesPasswordAndSaves() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded-pass");
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponseDto result = userService.create(new UserRequestDto("alice", "pass", "ROLE_USER"));

        assertThat(result.username()).isEqualTo("alice");
        assertThat(result.role()).isEqualTo("ROLE_USER");
        verify(passwordEncoder).encode("pass");
        verify(userRepository).save(any());
    }

    @Test
    void create_withNullRole_defaultsToRoleUser() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponseDto result = userService.create(new UserRequestDto("alice", "pass", null));

        assertThat(result.role()).isEqualTo("ROLE_USER");
    }

    @Test
    void create_withTakenUsername_throwsException() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(new User(1L, "alice", "encoded", "ROLE_USER")));

        assertThatThrownBy(() -> userService.create(new UserRequestDto("alice", "pass", "ROLE_USER")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("alice");
    }

    @Test
    void update_whenExists_updatesFields() {
        User existing = new User(1L, "alice", "old-encoded", "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpass")).thenReturn("new-encoded");
        when(userRepository.save(existing)).thenReturn(existing);

        UserResponseDto result = userService.update(1L, new UserRequestDto("alice-new", "newpass", "ROLE_ADMIN"));

        assertThat(result.username()).isEqualTo("alice-new");
        assertThat(result.role()).isEqualTo("ROLE_ADMIN");
        verify(passwordEncoder).encode("newpass");
    }

    @Test
    void update_whenNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, new UserRequestDto("x", "y", "ROLE_USER")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_whenExists_callsDeleteById() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_whenNotFound_throwsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
        verify(userRepository, never()).deleteById(any());
    }
}
