package co.edu.ut.hotel.service;

import co.edu.ut.hotel.service.dto.request.DisponibilidadRequest;
import co.edu.ut.hotel.service.dto.response.CotizacionResponse;
import co.edu.ut.hotel.service.dto.response.HabitacionDisponibleResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato del servicio de disponibilidad.
 * Consulta qué habitaciones están libres para un rango de fechas y genera cotizaciones.
 */
public interface DisponibilidadService {

    /**
     * Busca habitaciones disponibles para un rango de fechas, filtro opcional de tipo y personas.
     * Incluye el valor total estimado de cada una.
     *
     * @param checkIn  fecha de entrada
     * @param checkOut fecha de salida
     * @param type     tipo de habitacion (opcional)
     * @param guests   cantidad de personas (opcional)
     * @return lista de habitaciones disponibles con su valor estimado
     */
    List<HabitacionDisponibleResponse> findAvailable(LocalDate checkIn, LocalDate checkOut, String type, Integer guests);

    /**
     * Genera una cotizacion para una habitacion y un rango de fechas.
     *
     * @param roomId identificador de la habitacion
     * @param checkIn fecha de entrada
     * @param checkOut fecha de salida
     * @return cotizacion con desglose del valor
     */
    CotizacionResponse quote(Long roomId, LocalDate checkIn, LocalDate checkOut);

    /**
     * Lista todas las habitaciones que se pueden reservar (no en MANTENIMIENTO).
     * Se usa en formularios para mostrar todas las opciones reservables.
     *
     * @return lista de habitaciones reservables
     */
    List<HabitacionDisponibleResponse> findAllReservables();
}
