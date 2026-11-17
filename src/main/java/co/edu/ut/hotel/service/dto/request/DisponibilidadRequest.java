package co.edu.ut.hotel.service.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para consultar disponibilidad de habitaciones.
 */
@Getter
@Setter
@NoArgsConstructor
public class DisponibilidadRequest {

    @NotNull(message = "La fecha de entrada es obligatoria")
    @FutureOrPresent(message = "La fecha de entrada no puede ser anterior a hoy")
    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate fechaEntrada;

    @NotNull(message = "La fecha de salida es obligatoria")
    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate fechaSalida;

    private String tipo;

    private Integer personas;

    @AssertTrue(message = "La fecha de salida debe ser posterior a la de entrada")
    public boolean isFechasValidas() {
        if (fechaEntrada == null || fechaSalida == null) {
            return true;
        }
        return fechaSalida.isAfter(fechaEntrada);
    }
}