package co.edu.ut.hotel.service.dto.response;

import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;

import java.math.BigDecimal;

/**
 * DTO de salida con una habitacion disponible y su valor total estimado.
 */
public record HabitacionDisponibleResponse(
        Long id,
        String numero,
        TipoHabitacion tipo,
        Integer capacidad,
        BigDecimal tarifaNoche,
        EstadoHabitacion estado,
        BigDecimal valorTotalEstimado
) {
}
