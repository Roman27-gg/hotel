package co.edu.ut.hotel.service.dto.response;

import co.edu.ut.hotel.service.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * DTO de salida con los datos de una reserva listos para mostrar en la vista.
 * Incluye el desglose del valor de la estadia.
 */
public record ReservaResponse(
        Long id,
        Long habitacionId,
        Long huespedId,
        String habitacionNumero,
        String huespedNombreCompleto,
        String huespedDocumento,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        Integer noches,
        Integer numHuespedes,
        EstadoReserva estado,
        BigDecimal valorTotal,
        BigDecimal penalizacion,
        Map<String, BigDecimal> desglose
) {
}
