package co.edu.ut.hotel.service;

import co.edu.ut.hotel.service.dto.response.CotizacionResponse;

/**
 * Contrato del servicio de cotizacion.
 * Calcula el valor de una estadia usando el metodo polimorfico de la habitacion.
 */
public interface CotizacionService {

    /**
     * Calcula el valor de una estadia para una habitacion y una cantidad de noches.
     *
     * @param roomId identificador de la habitacion
     * @param nights  numero de noches
     * @return cotizacion con subtotal, recargo, descuento y total
     */
    CotizacionResponse calculate(Long roomId, int nights);
}
