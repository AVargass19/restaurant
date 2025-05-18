package com.restaurant.dto;

import com.restaurant.model.RestaurantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar una mesa del restaurante")
public class RestaurantTableDto {

    @Schema(description = "Identificador único de la mesa", example = "1")
    private Long id;

    @NotNull(message = "El estado de la mesa es obligatorio")
    @Schema(description = "Estado actual de la mesa", example = "AVAILABLE")
    private RestaurantTable.TableStatus status;

}