package com.restaurant.dto;

import com.restaurant.model.Reservation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar una reserva")
public class ReservationDto {

    @Schema(description = "Identificador único de la reserva", example = "1")
    private Long id;

    @Schema(description = "ID del usuario que realiza la reserva", example = "1")
    private Long userId;

    @NotNull(message = "La mesa es obligatoria")
    @Schema(description = "ID de la mesa reservada", example = "1")
    private Long tableId;

    @NotNull(message = "La fecha y hora son obligatorias")
    @Future(message = "La fecha y hora deben ser en el futuro")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Schema(description = "Fecha y hora de la reserva", example = "2025-06-15T19:00:00")
    private LocalDateTime date;

    @NotNull(message = "El número de invitados es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 invitado")
    @Schema(description = "Número de personas de la reserva", example = "4")
    private Integer guests;

    @Schema(description = "Estado de la reserva", example = "ACTIVE")
    private Reservation.ReservationStatus status;
}
