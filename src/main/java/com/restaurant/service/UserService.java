package com.restaurant.service;

import com.restaurant.model.Role;
import com.restaurant.model.User;
import com.restaurant.dto.UserDto;
import com.restaurant.repository.RoleRepository;
import com.restaurant.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public long countAll() {
        return userRepository.count();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setLastName(userDto.getLastName());
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setPhone(userDto.getPhone());
        user.setEnabled(userDto.getEnabled() != null ? userDto.getEnabled() : true);

        // Asignar rol
        Role role = roleRepository.findByName(userDto.getRole())
                .orElseGet(() -> roleRepository.findByName("ROLE_USER").orElseThrow());
        user.setRoles(new HashSet<>(Collections.singletonList(role)));

        return userRepository.save(user);
    }

    public User update(UserDto userDto) {
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setName(userDto.getName());
        user.setLastName(userDto.getLastName());
        user.setPhone(userDto.getPhone());
        user.setEnabled(userDto.getEnabled() != null ? userDto.getEnabled() : true);

        // Actualizar contraseña solo si no está vacía
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        // Actualizar rol si ha cambiado
        if (userDto.getRole() != null && !userDto.getRole().isEmpty()) {
            Role role = roleRepository.findByName(userDto.getRole())
                    .orElseGet(() -> roleRepository.findByName("ROLE_USER").orElseThrow());
            user.setRoles(new HashSet<>(Collections.singletonList(role)));
        }

        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }
}
