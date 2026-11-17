package co.edu.ut.hotel.service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para crear o actualizar una habitacion.
 */
@Getter
@Setter
@NoArgsConstructor
public class HabitacionRequest {

    @NotBlank(message = "El numero de habitacion es obligatorio")
    private String numero;

    @NotNull(message = "El tipo de habitacion es obligatorio")
    private String tipo;

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser mayor a cero")
    @jakarta.validation.constraints.Max(value = 6, message = "La capacidad maxima es 6 personas")
    private Integer capacidad;

    @NotNull(message = "La tarifa por noche es obligatoria")
    @DecimalMin(value = "0.01", message = "La tarifa por noche debe ser mayor a cero")
    private BigDecimal tarifaNoche;

    @NotBlank(message = "El estado es obligatorio")
    private String estado;
}
