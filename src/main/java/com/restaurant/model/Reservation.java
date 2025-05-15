package com.restaurant.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservations")
@Schema(description = "Modelo que representa una reserva en el restaurante")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "res_id")
    @Schema(description = "Identificador único de la reserva", example = "1")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Usuario que realiza la reserva")
    private User user;

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    @Schema(description = "Mesa reservada")
    private RestaurantTable table;

    @Column(name = "res_date", nullable = false)
    @Schema(description = "Fecha y hora de la reserva", example = "2025-06-15T19:00:00")
    private LocalDateTime date;

    @Column(name = "res_guests", nullable = false)
    @Schema(description = "Número de personas de la reserva", example = "4")
    private Integer guests;

    @Column(name = "res_estado", nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Estado de la reserva", example = "ACTIVE")
    private ReservationStatus status;

    @Column(name = "res_created_at", updatable = false)
    @Schema(description = "Fecha y hora de creación de la reserva", example = "2025-05-01T12:00:00")
    private LocalDateTime createdAt;

    public enum ReservationStatus {
        ACTIVE,
        CANCELLED,
        COMPLETED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}