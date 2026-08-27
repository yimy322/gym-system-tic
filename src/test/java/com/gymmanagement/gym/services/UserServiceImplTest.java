package com.gymmanagement.gym.services;

import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.gymmanagement.gym.entities.Role;
import com.gymmanagement.gym.entities.User;
import com.gymmanagement.gym.repository.UserRepository;
import com.gymmanagement.gym.services.impl.UserServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("ROLE_ADMIN");

        user = new User();
        user.setUsername("jperez");
        user.setPassword("encoded-password");
        user.setEnabled(true);
        user.setRoles(Set.of(role));
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        when(userRepository.findByUsername("jperez")).thenReturn(Optional.of(user));
        UserDetails result = userService.loadUserByUsername("jperez");
        // assert
        assertThat(result.getUsername()).isEqualTo("jperez");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findByUsername("noexiste")).thenReturn(Optional.empty());
        // assert
        assertThatThrownBy(() -> userService.loadUserByUsername("noexiste"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("noexiste");
    }

}
