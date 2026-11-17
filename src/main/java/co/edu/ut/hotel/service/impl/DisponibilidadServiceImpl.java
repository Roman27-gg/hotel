package co.edu.ut.hotel.service.impl;

import co.edu.ut.hotel.service.CotizacionService;
import co.edu.ut.hotel.service.DisponibilidadService;
import co.edu.ut.hotel.service.repository.HabitacionDao;
import co.edu.ut.hotel.service.repository.ReservaDao;
import co.edu.ut.hotel.service.dto.response.CotizacionResponse;
import co.edu.ut.hotel.service.dto.response.HabitacionDisponibleResponse;
import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.util.HabitacionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de disponibilidad.
 * Consulta qué habitaciones están libres para un rango de fechas y genera cotizaciones.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DisponibilidadServiceImpl implements DisponibilidadService {

    private final HabitacionDao habitacionDao;
    private final ReservaDao reservaDao;
    private final CotizacionService cotizacionService;

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionDisponibleResponse> findAvailable(LocalDate checkIn, LocalDate checkOut, String type, Integer guests) {
        List<Habitacion> todas = habitacionDao.findAll();
        List<HabitacionDisponibleResponse> disponibles = new ArrayList<>();
        for (Habitacion h : todas) {
            if (EstadoHabitacion.MANTENIMIENTO.equals(h.getEstado())) {
                continue;
            }
            if (type != null && !type.isEmpty() && !h.getTipo().name().equals(type)) {
                continue;
            }
            if (guests != null && !HabitacionUtil.admite(guests, h.getCapacidad())) {
                continue;
            }
            if (reservaDao.existsOverlap(h.getId(), checkIn, checkOut)) {
                continue;
            }
            int noches = HabitacionUtil.calcularNoches(checkIn, checkOut);
            BigDecimal valorTotal = cotizacionService.calculate(h.getId(), noches).total();
            disponibles.add(new HabitacionDisponibleResponse(
                    h.getId(),
                    h.getNumero(),
                    h.getTipo(),
                    h.getCapacidad(),
                    h.getTarifaNoche(),
                    h.getEstado(),
                    valorTotal
            ));
        }
        return disponibles;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionDisponibleResponse> findAllReservables() {
        List<Habitacion> todas = habitacionDao.findAll();
        List<HabitacionDisponibleResponse> reservables = new ArrayList<>();
        for (Habitacion h : todas) {
            if (EstadoHabitacion.MANTENIMIENTO.equals(h.getEstado())) {
                continue;
            }
            // Para el formulario sin fechas, usar 1 noche como estimación
            BigDecimal valorTotal = cotizacionService.calculate(h.getId(), 1).total();
            reservables.add(new HabitacionDisponibleResponse(
                    h.getId(),
                    h.getNumero(),
                    h.getTipo(),
                    h.getCapacidad(),
                    h.getTarifaNoche(),
                    h.getEstado(),
                    valorTotal
            ));
        }
        return reservables;
    }

    @Override
    @Transactional(readOnly = true)
    public CotizacionResponse quote(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        int noches = HabitacionUtil.calcularNoches(checkIn, checkOut);
        return cotizacionService.calculate(roomId, noches);
    }
}
