package com.restaurant.service;

import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.User;
import com.restaurant.dto.ReservationDto;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.RestaurantTableRepository;
import com.restaurant.repository.UserRepository;
import com.restaurant.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RestaurantTableRepository tableRepository;
    private final RestaurantTableService tableService;

    public ReservationService(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            RestaurantTableRepository tableRepository,
            RestaurantTableService tableService) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.tableRepository = tableRepository;
        this.tableService = tableService;
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    public List<Reservation> findByUser(User user) {
        return reservationRepository.findByUser(user);
    }

    public List<Reservation> findByCurrentUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        return reservationRepository.findByUser(user);
    }

    public List<Reservation> findByUserAndStatus(User user, Reservation.ReservationStatus status) {
        return reservationRepository.findByUserAndStatus(user, status);
    }

    public List<Reservation> findByStatus(Reservation.ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }

    // Método para encontrar las reservas de hoy para el dashboard
    public List<Reservation> findReservationsForToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        return reservationRepository.findAll().stream()
                .filter(r -> r.getDate().isAfter(startOfDay) && r.getDate().isBefore(endOfDay))
                .collect(Collectors.toList());
    }

    // Método para contar reservas activas para estadísticas del dashboard
    public long countActiveReservations() {
        return reservationRepository.findByStatus(Reservation.ReservationStatus.ACTIVE).size();
    }

    // Método para contar reservas de hoy para estadísticas del dashboard
    public long countTodayReservations() {
        return findReservationsForToday().size();
    }

    // Método para encontrar las reservas más recientes (limitadas por cantidad)
    public List<Reservation> findRecentReservations(int limit) {
        // Obtener todas las reservas y ordenarlas por fecha descendente
        List<Reservation> allReservations = reservationRepository.findAll();
        return allReservations.stream()
                .sorted((r1, r2) -> r2.getDate().compareTo(r1.getDate())) // Ordenar por fecha descendente
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional
    public Reservation create(ReservationDto reservationDto) {
        // Obtener usuario
        User user;
        if (reservationDto.getUserId() != null) {
            user = userRepository.findById(reservationDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        } else {
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            user = userRepository.findById(userDetails.getId()).orElseThrow();
        }

        // Obtener mesa
        RestaurantTable table = tableRepository.findById(reservationDto.getTableId())
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        // Verificar disponibilidad de la mesa
        if (table.getStatus() != RestaurantTable.TableStatus.AVAILABLE) {
            throw new RuntimeException("La mesa no está disponible");
        }

        // Verificar si ya existe una reserva para esa mesa en ese día
        LocalDateTime startOfDay = reservationDto.getDate().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = reservationDto.getDate().toLocalDate().atTime(23, 59, 59);

        List<Reservation> existingReservations = reservationRepository.findByTableAndDateBetweenAndStatus(
                table.getId(), startOfDay, endOfDay, Reservation.ReservationStatus.ACTIVE);

        if (!existingReservations.isEmpty()) {
            throw new RuntimeException("Ya existe una reserva para esta mesa en este día");
        }

        // Crear la reserva
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setTable(table);
        reservation.setDate(reservationDto.getDate());
        reservation.setGuests(reservationDto.getGuests());
        reservation.setStatus(Reservation.ReservationStatus.ACTIVE);

        // Actualizar estado de la mesa
        table.setStatus(RestaurantTable.TableStatus.RESERVED);
        tableRepository.save(table);

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation update(ReservationDto reservationDto) {
        // Verificar que la reserva existe
        Reservation reservation = reservationRepository.findById(reservationDto.getId())
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Verificar permiso para modificar la reserva
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStaff = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

        if (!isAdmin && !isStaff && !reservation.getUser().getId().equals(userDetails.getId())) {
            throw new AccessDeniedException("No tienes permiso para modificar esta reserva");
        }

        // Verificar restricciones de tiempo para usuarios normales
        if (!isAdmin && !isStaff) {
            long hoursUntilReservation = LocalDateTime.now().until(reservation.getDate(), ChronoUnit.HOURS);
            if (hoursUntilReservation < 5) {
                throw new RuntimeException("No se puede modificar la reserva menos de 5 horas antes");
            }
        }

        // Si cambia la mesa, verificar disponibilidad
        if (!reservation.getTable().getId().equals(reservationDto.getTableId())) {
            RestaurantTable newTable = tableRepository.findById(reservationDto.getTableId())
                    .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

            if (newTable.getStatus() != RestaurantTable.TableStatus.AVAILABLE) {
                throw new RuntimeException("La mesa seleccionada no está disponible");
            }

            // Liberar mesa anterior
            RestaurantTable oldTable = reservation.getTable();
            oldTable.setStatus(RestaurantTable.TableStatus.AVAILABLE);
            tableRepository.save(oldTable);

            // Reservar nueva mesa
            newTable.setStatus(RestaurantTable.TableStatus.RESERVED);
            tableRepository.save(newTable);

            reservation.setTable(newTable);
        }

        // Actualizar campos de la reserva
        reservation.setDate(reservationDto.getDate());
        reservation.setGuests(reservationDto.getGuests());

        if (reservationDto.getStatus() != null) {
            reservation.setStatus(reservationDto.getStatus());

            // Si la reserva se cancela, liberar la mesa
            if (reservationDto.getStatus() == Reservation.ReservationStatus.CANCELLED) {
                RestaurantTable table = reservation.getTable();
                table.setStatus(RestaurantTable.TableStatus.AVAILABLE);
                tableRepository.save(table);
            }
        }

        return reservationRepository.save(reservation);
    }

    @Transactional
    public void cancel(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Verificar permiso para cancelar la reserva
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStaff = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

        if (!isAdmin && !isStaff && !reservation.getUser().getId().equals(userDetails.getId())) {
            throw new AccessDeniedException("No tienes permiso para cancelar esta reserva");
        }

        // Verificar restricciones de tiempo para usuarios normales
        if (!isAdmin && !isStaff) {
            long hoursUntilReservation = LocalDateTime.now().until(reservation.getDate(), ChronoUnit.HOURS);
            if (hoursUntilReservation < 5) {
                throw new RuntimeException("No se puede cancelar la reserva menos de 5 horas antes");
            }
        }

        // Actualizar estado de la reserva
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        // Liberar la mesa
        RestaurantTable table = reservation.getTable();
        table.setStatus(RestaurantTable.TableStatus.AVAILABLE);
        tableRepository.save(table);
    }

    @Transactional
    public void complete(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Solo admins y staff pueden marcar como completada
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStaff = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

        if (!isAdmin && !isStaff) {
            throw new AccessDeniedException("No tienes permiso para completar esta reserva");
        }

        // Actualizar estado de la reserva
        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);

        // Liberar la mesa
        RestaurantTable table = reservation.getTable();
        table.setStatus(RestaurantTable.TableStatus.AVAILABLE);
        tableRepository.save(table);
    }
}