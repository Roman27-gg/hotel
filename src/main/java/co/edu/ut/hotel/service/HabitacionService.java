package co.edu.ut.hotel.service;

import co.edu.ut.hotel.service.dto.request.HabitacionRequest;
import co.edu.ut.hotel.service.dto.response.HabitacionResponse;

import java.util.List;

/**
 * Contrato del servicio de habitaciones.
 * Expone operaciones de CRUD y consulta filtrada; el controlador solo usa DTOs.
 */
public interface HabitacionService {

    /**
     * Lista todas las habitaciones.
     *
     * @return lista de respuestas de habitacion
     */
    List<HabitacionResponse> findAll();

    /**
     * Busca una habitacion por su identificador.
     *
     * @param id identificador de la habitacion
     * @return respuesta de la habitacion encontrada
     * @throws co.edu.ut.hotel.service.exception.RecursoNoEncontradoException si no existe
     */
    HabitacionResponse findById(Long id);

    /**
     * Crea una nueva habitacion.
     *
     * @param request datos de la habitacion
     * @return respuesta de la habitacion creada
     */
    HabitacionResponse create(HabitacionRequest request);

    /**
     * Actualiza una habitacion existente.
     *
     * @param id      identificador de la habitacion
     * @param request datos actualizados
     * @return respuesta de la habitacion actualizada
     */
    HabitacionResponse update(Long id, HabitacionRequest request);

    /**
     * Elimina una habitacion si no tiene reservas asociadas.
     *
     * @param id identificador de la habitacion
     * @throws co.edu.ut.hotel.service.exception.HabitacionConReservasException si tiene reservas
     */
    void delete(Long id);

    /**
     * Busca habitaciones por tipo y estado.
     *
     * @param type  tipo de habitacion
     * @param state estado de la habitacion
     * @return lista de respuestas de habitacion
     */
    List<HabitacionResponse> findByTypeAndState(String type, String state);
}
