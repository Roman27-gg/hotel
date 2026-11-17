package co.edu.ut.hotel.service.dto.response;

import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;

import java.math.BigDecimal;

/**
 * DTO de salida con los datos de una habitacion listos para mostrar en la vista.
 */
public record HabitacionResponse(
        Long id,
        String numero,
        TipoHabitacion tipo,
        Integer capacidad,
        BigDecimal tarifaNoche,
        EstadoHabitacion estado
) {
}
