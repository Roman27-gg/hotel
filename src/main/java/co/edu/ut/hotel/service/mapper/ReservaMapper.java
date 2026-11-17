package co.edu.ut.hotel.service.mapper;

import co.edu.ut.hotel.service.dto.request.ReservaRequest;
import co.edu.ut.hotel.service.dto.response.ReservaResponse;
import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.entity.Huesped;
import co.edu.ut.hotel.service.entity.Reserva;
import co.edu.ut.hotel.service.enums.EstadoReserva;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import co.edu.ut.hotel.service.util.HabitacionUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper que convierte entre la entidad Reserva y su DTO de respuesta,
 * y entre el request y la entidad.
 */
@Component
public class ReservaMapper {

    /**
     * Convierte un ReservaRequest y las entidades cargadas en una entidad Reserva.
     *
     * @param request    datos de entrada
     * @param habitacion habitacion cargada desde el DAO
     * @param huesped    huesped cargado desde el DAO
     * @return entidad Reserva lista para persistir
     */
    public Reserva toEntity(ReservaRequest request, Habitacion habitacion, Huesped huesped) {
        Reserva reserva = new Reserva();
        reserva.setHabitacion(habitacion);
        reserva.setHuesped(huesped);
        reserva.setFechaEntrada(request.getFechaEntrada());
        reserva.setFechaSalida(request.getFechaSalida());
        reserva.setNumHuespedes(request.getNumHuespedes());
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setValorTotal(request.getValorTotal() == null ? BigDecimal.ZERO : request.getValorTotal());
        reserva.setPenalizacion(BigDecimal.ZERO);
        return reserva;
    }

    /**
     * Convierte una entidad en su DTO de respuesta.
     * El desglose se obtiene de HabitacionUtil según el tipo de habitacion.
     *
     * @param entidad entidad de dominio
     * @return DTO de respuesta
     */
    public ReservaResponse toResponse(Reserva entidad) {
        Habitacion habitacion = entidad.getHabitacion();
        int noches = HabitacionUtil.calcularNoches(entidad.getFechaEntrada(), entidad.getFechaSalida());
        Map<String, BigDecimal> desglose;
        if (habitacion.getTipo() == TipoHabitacion.SENCILLA) {
            desglose = HabitacionUtil.desglosarSencilla(habitacion.getTarifaNoche(), noches);
        } else {
            desglose = HabitacionUtil.desglosarSuite(habitacion.getTarifaNoche(), noches);
        }
        return new ReservaResponse(
                entidad.getId(),
                entidad.getHabitacion().getId(),
                entidad.getHuesped().getId(),
                habitacion.getNumero(),
                entidad.getHuesped().getNombreCompleto(),
                entidad.getHuesped().getDocumento(),
                entidad.getFechaEntrada(),
                entidad.getFechaSalida(),
                noches,
                entidad.getNumHuespedes(),
                entidad.getEstado(),
                entidad.getValorTotal(),
                entidad.getPenalizacion(),
                desglose
        );
    }

    /**
     * Convierte una lista de entidades en una lista de DTOs de respuesta.
     *
     * @param entidades lista de entidades
     * @return lista de DTOs
     */
    public List<ReservaResponse> toResponseList(List<Reserva> entidades) {
        return entidades.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
