package co.edu.ut.hotel.service.dto.response;

import java.math.BigDecimal;

/**
 * DTO de salida con el desglose del valor de una estadia (cotizacion).
 */
public record CotizacionResponse(
        Integer noches,
        BigDecimal subtotal,
        BigDecimal recargo,
        BigDecimal descuento,
        BigDecimal total
) {
}
