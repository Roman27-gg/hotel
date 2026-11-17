package co.edu.ut.hotel.service.mapper;

import co.edu.ut.hotel.service.dto.request.HuespedRequest;
import co.edu.ut.hotel.service.dto.response.HuespedResponse;
import co.edu.ut.hotel.service.entity.Huesped;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper que convierte entre la entidad Huesped y su DTO de respuesta,
 * y entre el request y la entidad.
 */
@Component
public class HuespedMapper {

    /**
     * Convierte un HuespedRequest en una entidad Huesped.
     *
     * @param request datos de entrada
     * @return entidad Huesped lista para persistir
     */
    public Huesped toEntity(HuespedRequest request) {
        Huesped huesped = new Huesped();
        huesped.setDocumento(request.getDocumento());
        huesped.setNombre(request.getNombre());
        huesped.setApellido(request.getApellido());
        huesped.setTelefono(request.getTelefono());
        huesped.setEmail(request.getEmail());
        huesped.setFechaRegistro(LocalDateTime.now());
        return huesped;
    }

    /**
     * Convierte una entidad en su DTO de respuesta.
     *
     * @param entidad entidad de dominio
     * @return DTO de respuesta
     */
    public HuespedResponse toResponse(Huesped entidad) {
        return new HuespedResponse(
                entidad.getId(),
                entidad.getDocumento(),
                entidad.getNombre(),
                entidad.getApellido(),
                entidad.getTelefono(),
                entidad.getEmail()
        );
    }

    /**
     * Convierte una lista de entidades en una lista de DTOs de respuesta.
     *
     * @param entidades lista de entidades
     * @return lista de DTOs
     */
    public List<HuespedResponse> toResponseList(List<Huesped> entidades) {
        return entidades.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
