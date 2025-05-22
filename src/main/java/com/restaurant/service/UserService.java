package com.restaurant.service;

import com.restaurant.model.Role;
import com.restaurant.model.User;
import com.restaurant.dto.UserDto;
import com.restaurant.repository.RoleRepository;
import com.restaurant.repository.UserRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    public void changePassword(String currentPassword, String newPassword) {
        // Obtener el usuario actual
        User currentUser = getCurrentUser();

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        // Validar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(newPassword, currentUser.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }

        // Validar longitud mínima
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres");
        }

        // Actualizar la contraseña
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);
    }

    @Autowired
    private ResourceLoader resourceLoader;

    public byte[] generateUsersReport(List<User> users) throws Exception {
        try {
            // Cargar el archivo .jrxml desde resources/reports
            Resource resource = resourceLoader.getResource("classpath:reports/users_report.jrxml");
            InputStream inputStream = resource.getInputStream();

            // Compilar el reporte
            JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

            // Crear el datasource con los usuarios
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(users);

            // Parámetros del reporte
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "Reporte de Usuarios");
            parameters.put("generatedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            parameters.put("totalUsers", users.size());

            // Llenar el reporte
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Exportar a PDF
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            throw new RuntimeException("Error generando reporte de usuarios: " + e.getMessage(), e);
        }
    }
}
