package com.restaurant.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "historial_res")
@Schema(description = "Modelo que representa un registro en el historial de reservas")
public class ReservationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "his_id")
    @Schema(description = "Identificador único del registro de historial", example = "1")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Usuario que realizó la acción")
    private User user;

    @ManyToOne
    @JoinColumn(name = "res_id", nullable = false)
    @Schema(description = "Reserva afectada por la acción")
    private Reservation reservation;

    @Column(name = "his_action", nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Tipo de acción realizada", example = "CREATE")
    private ActionType action;

    @Column(name = "his_old_values", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Schema(description = "Valores anteriores de la reserva (en formato JSON)")
    private String oldValues;

    @Column(name = "his_new_values", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Schema(description = "Valores nuevos de la reserva (en formato JSON)")
    private String newValues;

    @Column(name = "his_created_at", updatable = false)
    @Schema(description = "Fecha y hora de la acción", example = "2025-05-01T12:00:00")
    private LocalDateTime createdAt;

    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}