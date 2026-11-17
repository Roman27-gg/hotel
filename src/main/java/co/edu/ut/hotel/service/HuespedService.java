package co.edu.ut.hotel.service;

import co.edu.ut.hotel.service.dto.request.HuespedRequest;
import co.edu.ut.hotel.service.dto.response.HuespedResponse;

import java.util.List;

/**
 * Contrato del servicio de huespedes.
 * Expone operaciones de agregado y consulta; la edicion es opcional en el alcance minimo.
 */
public interface HuespedService {

    /**
     * Lista todos los huespedes.
     *
     * @return lista de respuestas de huesped
     */
    List<HuespedResponse> findAll();

    /**
     * Busca un huesped por su identificador.
     *
     * @param id identificador del huesped
     * @return respuesta del huesped encontrado
     */
    HuespedResponse findById(Long id);

    /**
     * Crea un nuevo huesped.
     *
     * @param request datos del huesped
     * @return respuesta del huesped creado
     */
    HuespedResponse create(HuespedRequest request);

    /**
     * Busca un huesped por su numero de documento.
     *
     * @param document numero de documento
     * @return respuesta del huesped encontrado
     * @throws co.edu.ut.hotel.service.exception.RecursoNoEncontradoException si no existe
     */
    HuespedResponse findByDocument(String document);
}
