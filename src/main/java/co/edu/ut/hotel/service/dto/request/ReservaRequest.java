package co.edu.ut.hotel.service.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para crear una reserva.
 * El campo simularFallo solo tiene efecto en el perfil demo cuando hotel.demo.simular-fallo-habilitado=true.
 */
@Getter
@Setter
@NoArgsConstructor
public class ReservaRequest {

    @NotNull(message = "La habitacion es obligatoria")
    private Long habitacionId;

    @NotNull(message = "El huesped es obligatorio")
    private Long huespedId;

    @NotNull(message = "La fecha de entrada es obligatoria")
    @FutureOrPresent(message = "La fecha de entrada no puede ser anterior a hoy")
    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate fechaEntrada;

    @NotNull(message = "La fecha de salida es obligatoria")
    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate fechaSalida;

    @NotNull(message = "El numero de huespedes es obligatorio")
    @Positive(message = "El numero de huespedes debe ser mayor a cero")
    private Integer numHuespedes;

    private Boolean simularFallo;

    @DecimalMin(value = "0.0", message = "El valor total no puede ser negativo")
    private BigDecimal valorTotal;

    /**
     * Valida que la fecha de salida sea posterior a la de entrada.
     *
     * @return true si las fechas son validas
     */
    @AssertTrue(message = "La fecha de salida debe ser posterior a la de entrada")
    public boolean isFechasValidas() {
        if (fechaEntrada == null || fechaSalida == null) {
            return true;
        }
        return fechaSalida.isAfter(fechaEntrada);
    }
}