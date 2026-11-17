package co.edu.ut.hotel.service;

import co.edu.ut.hotel.service.dto.request.ReservaRequest;
import co.edu.ut.hotel.service.dto.response.ReservaResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato del servicio de reservas.
 * Incluye la transaccion obligatoria de creacion de reserva (verificar disponibilidad y cambiar estado),
 * cancelacion y check-in/check-out.
 */
public interface ReservaService {

    /**
     * Crea una reserva en una transaccion que toca dos tablas (reserva y habitacion).
     * Verifica que la habitacion este libre en el rango de fechas antes de guardar.
     * Si hay cruce, no se guarda nada.
     *
     * @param request datos de la reserva
     * @return respuesta de la reserva creada
     */
    ReservaResponse create(ReservaRequest request);

    /**
     * Actualiza una reserva existente permitiendo cambiar fechaEntrada, fechaSalida y numHuespedes.
     * Verifica que las nuevas fechas no crucen con otra reserva confirmada de la misma habitación.
     * No permite cambiar la fecha de entrada a una fecha pasada.
     *
     * @param id      identificador de la reserva
     * @param request datos actualizados de la reserva
     * @return respuesta de la reserva actualizada
     */
    ReservaResponse update(Long id, ReservaRequest request);

    /**
     * Lista todas las reservas.
     *
     * @return lista de respuestas de reserva
     */
    List<ReservaResponse> findAll();

    /**
     * Busca una reserva por su identificador.
     *
     * @param id identificador de la reserva
     * @return respuesta de la reserva encontrada
     */
    ReservaResponse findById(Long id);

    /**
     * Cancela una reserva confirmada en una transaccion (reserva + estado de habitacion).
     * Calcula la penalizacion segun la politica de cancelacion.
     *
     * @param id identificador de la reserva
     */
    void cancel(Long id);

    /**
     * Registra el check-in de una reserva. La habitacion pasa a estado OCUPADA.
     *
     * @param reservaId identificador de la reserva
     */
    void checkIn(Long reservaId);

    /**
     * Registra el check-out de una reserva. La reserva pasa a FINALIZADA y la habitacion queda disponible.
     *
     * @param reservaId identificador de la reserva
     */
    void checkOut(Long reservaId);

    /**
     * Busca reservas por estado y rango de fechas.
     *
     * @param state estado de la reserva
     * @param from  fecha desde
     * @param to    fecha hasta
     * @return lista de respuestas de reserva
     */
    List<ReservaResponse> findByStateAndDateRange(String state, LocalDate from, LocalDate to);
}
