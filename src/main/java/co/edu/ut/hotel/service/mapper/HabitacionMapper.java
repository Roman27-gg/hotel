package co.edu.ut.hotel.service.mapper;

import co.edu.ut.hotel.service.dto.request.HabitacionRequest;
import co.edu.ut.hotel.service.dto.response.HabitacionResponse;
import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.entity.HabitacionSencilla;
import co.edu.ut.hotel.service.entity.HabitacionSuite;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper que convierte entre la entidad Habitacion y su DTO de respuesta,
 * y entre el request y la entidad.
 * Decide la subclase concreta (Sencilla o Suite) según el tipo.
 */
@Component
public class HabitacionMapper {

    /**
     * Convierte un HabitacionRequest en una entidad Habitacion.
     * Crea HabitacionSencilla o HabitacionSuite según el tipo indicado.
     *
     * @param request datos de entrada
     * @return entidad Habitacion lista para persistir
     */
    public Habitacion toEntity(HabitacionRequest request) {
        TipoHabitacion tipo = TipoHabitacion.valueOf(request.getTipo());
        EstadoHabitacion estado = EstadoHabitacion.valueOf(request.getEstado());
        if (tipo == TipoHabitacion.SENCILLA) {
            return new HabitacionSencilla(
                    null, request.getNumero(), request.getCapacidad(), request.getTarifaNoche(), estado);
        } else {
            return new HabitacionSuite(
                    null, request.getNumero(), request.getCapacidad(), request.getTarifaNoche(), estado);
        }
    }

    /**
     * Convierte una entidad en su DTO de respuesta.
     *
     * @param entidad entidad de dominio
     * @return DTO de respuesta
     */
    public HabitacionResponse toResponse(Habitacion entidad) {
        return new HabitacionResponse(
                entidad.getId(),
                entidad.getNumero(),
                entidad.getTipo(),
                entidad.getCapacidad(),
                entidad.getTarifaNoche(),
                entidad.getEstado()
        );
    }

    /**
     * Convierte una lista de entidades en una lista de DTOs de respuesta.
     *
     * @param entidades lista de entidades
     * @return lista de DTOs
     */
    public List<HabitacionResponse> toResponseList(List<Habitacion> entidades) {
        return entidades.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
