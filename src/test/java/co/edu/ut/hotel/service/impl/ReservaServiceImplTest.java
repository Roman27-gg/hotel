package co.edu.ut.hotel.service.impl;

import co.edu.ut.hotel.service.ReservaService;
import co.edu.ut.hotel.service.CotizacionService;
import co.edu.ut.hotel.service.dto.request.ReservaRequest;
import co.edu.ut.hotel.service.dto.response.ReservaResponse;
import co.edu.ut.hotel.service.repository.HabitacionDao;
import co.edu.ut.hotel.service.repository.HuespedDao;
import co.edu.ut.hotel.service.repository.ReservaDao;
import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.entity.HabitacionSencilla;
import co.edu.ut.hotel.service.entity.Huesped;
import co.edu.ut.hotel.service.entity.Reserva;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.EstadoReserva;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import co.edu.ut.hotel.service.exception.HabitacionNoDisponibleException;
import co.edu.ut.hotel.service.exception.RecursoNoEncontradoException;
import co.edu.ut.hotel.service.exception.ReservaCruzadaException;
import co.edu.ut.hotel.service.mapper.ReservaMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de ReservaServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservaServiceImplTest {

    @Mock
    private ReservaDao reservaDao;

    @Mock
    private HabitacionDao habitacionDao;

    @Mock
    private HuespedDao huespedDao;

    @Mock
    private ReservaMapper reservaMapper;

    @Mock
    private CotizacionService cotizacionService;

    @Mock
    private Clock clock;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    @Test
    void createExitoso() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Huesped hu = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);

        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(1L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 20));
        req.setFechaSalida(LocalDate.of(2026, 11, 23));
        req.setNumHuespedes(2);

        Reserva entity = new Reserva();
        Reserva saved = new Reserva(1L, h, hu, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);
        ReservaResponse resp = new ReservaResponse(1L, 1L, 1L, "101", "Juan Perez", "12345678", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 3, 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, java.util.Map.of());

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(habitacionDao.lock(1L)).thenReturn(Optional.of(h));
        when(huespedDao.findById(1L)).thenReturn(Optional.of(hu));
        when(reservaDao.existsOverlap(1L, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23))).thenReturn(false);
        when(cotizacionService.calculate(1L, 3)).thenReturn(new co.edu.ut.hotel.service.dto.response.CotizacionResponse(3, new BigDecimal("240000.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("240000.00")));
        when(reservaMapper.toEntity(req, h, hu)).thenReturn(entity);
        when(reservaDao.save(entity)).thenReturn(saved);
        when(reservaMapper.toResponse(saved)).thenReturn(resp);

        ReservaResponse result = reservaService.create(req);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void createHabitacionNoEncontrada() {
        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(99L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 20));
        req.setFechaSalida(LocalDate.of(2026, 11, 23));
        req.setNumHuespedes(2);

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(habitacionDao.lock(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> reservaService.create(req));
    }

    @Test
    void createHabitacionMantenimiento() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.MANTENIMIENTO);
        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(1L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 20));
        req.setFechaSalida(LocalDate.of(2026, 11, 23));
        req.setNumHuespedes(2);

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(habitacionDao.lock(1L)).thenReturn(Optional.of(h));

        assertThrows(HabitacionNoDisponibleException.class, () -> reservaService.create(req));
    }

    @Test
    void createCruceFechas() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Huesped hu = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(1L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 20));
        req.setFechaSalida(LocalDate.of(2026, 11, 23));
        req.setNumHuespedes(2);

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(habitacionDao.lock(1L)).thenReturn(Optional.of(h));
        when(huespedDao.findById(1L)).thenReturn(Optional.of(hu));
        when(reservaDao.existsOverlap(1L, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23))).thenReturn(true);

        assertThrows(ReservaCruzadaException.class, () -> reservaService.create(req));
    }

    @Test
    void findAll() {
        when(reservaDao.findAll()).thenReturn(List.of());
        when(reservaMapper.toResponseList(any())).thenReturn(List.of());

        List<ReservaResponse> result = reservaService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdEncontrado() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Reserva r = new Reserva(1L, h, new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null), LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);
        when(reservaDao.findById(1L)).thenReturn(Optional.of(r));
        when(reservaMapper.toResponse(r)).thenReturn(new ReservaResponse(1L, 1L, 1L, "101", "Juan Perez", "12345678", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 3, 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, java.util.Map.of()));

        ReservaResponse result = reservaService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void findByIdNoEncontrado() {
        when(reservaDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> reservaService.findById(99L));
    }

    @Test
    void cancelExitoso() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.RESERVADA);
        Reserva r = new Reserva(1L, h, new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null), LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservaDao.findById(1L)).thenReturn(Optional.of(r));
        when(reservaDao.existsOverlap(1L, LocalDate.of(2026, 11, 21), LocalDate.of(2026, 11, 22))).thenReturn(false);

        reservaService.cancel(1L);

        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
        assertNotNull(r.getPenalizacion());
        assertNotNull(r.getFechaCancelacion());
    }

    @Test
    void checkInExitoso() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Reserva r = new Reserva(1L, h, new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null), LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservaDao.findById(1L)).thenReturn(Optional.of(r));

        reservaService.checkIn(1L);

        assertNotNull(r.getFechaCheckIn());
        assertEquals(EstadoHabitacion.OCUPADA, h.getEstado());
    }

    @Test
    void checkOutExitoso() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.OCUPADA);
        Reserva r = new Reserva(1L, h, new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null), LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);
        r.setFechaCheckIn(java.time.LocalDateTime.now());

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservaDao.findById(1L)).thenReturn(Optional.of(r));

        reservaService.checkOut(1L);

        assertNotNull(r.getFechaCheckOut());
        assertEquals(EstadoReserva.FINALIZADA, r.getEstado());
        assertEquals(EstadoHabitacion.DISPONIBLE, h.getEstado());
    }

    @Test
    void updateExitoso_cambioNumeroHuespedes() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Huesped hu = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        Reserva reservaExistente = new Reserva(1L, h, hu, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);

        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(1L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 20));
        req.setFechaSalida(LocalDate.of(2026, 11, 23));
        req.setNumHuespedes(1);

        Reserva saved = new Reserva(1L, h, hu, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 1, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);
        ReservaResponse resp = new ReservaResponse(1L, 1L, 1L, "101", "Juan Perez", "12345678", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 3, 1, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, java.util.Map.of());

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservaDao.findById(1L)).thenReturn(Optional.of(reservaExistente));
        when(habitacionDao.findById(1L)).thenReturn(Optional.of(h));
        when(huespedDao.findById(1L)).thenReturn(Optional.of(hu));
        when(reservaDao.existsOverlapExcluding(1L, 1L, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23))).thenReturn(false);
        when(cotizacionService.calculate(1L, 3)).thenReturn(new co.edu.ut.hotel.service.dto.response.CotizacionResponse(3, new BigDecimal("240000.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("240000.00")));
        when(reservaDao.save(reservaExistente)).thenReturn(saved);
        when(reservaMapper.toResponse(saved)).thenReturn(resp);

        ReservaResponse result = reservaService.update(1L, req);

        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.numHuespedes());
    }

    @Test
    void updateExitoso_cambioFechasSinCruce() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Huesped hu = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        Reserva reservaExistente = new Reserva(1L, h, hu, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);

        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(1L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 25));
        req.setFechaSalida(LocalDate.of(2026, 11, 28));
        req.setNumHuespedes(2);

        Reserva saved = new Reserva(1L, h, hu, LocalDate.of(2026, 11, 25), LocalDate.of(2026, 11, 28), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);
        ReservaResponse resp = new ReservaResponse(1L, 1L, 1L, "101", "Juan Perez", "12345678", LocalDate.of(2026, 11, 25), LocalDate.of(2026, 11, 28), 3, 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, java.util.Map.of());

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservaDao.findById(1L)).thenReturn(Optional.of(reservaExistente));
        when(habitacionDao.findById(1L)).thenReturn(Optional.of(h));
        when(huespedDao.findById(1L)).thenReturn(Optional.of(hu));
        when(reservaDao.existsOverlapExcluding(1L, 1L, LocalDate.of(2026, 11, 25), LocalDate.of(2026, 11, 28))).thenReturn(false);
        when(cotizacionService.calculate(1L, 3)).thenReturn(new co.edu.ut.hotel.service.dto.response.CotizacionResponse(3, new BigDecimal("240000.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("240000.00")));
        when(reservaDao.save(reservaExistente)).thenReturn(saved);
        when(reservaMapper.toResponse(saved)).thenReturn(resp);

        ReservaResponse result = reservaService.update(1L, req);

        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 11, 25), result.fechaEntrada());
        assertEquals(LocalDate.of(2026, 11, 28), result.fechaSalida());
    }

    @Test
    void updateFallo_reservaNoEncontrada() {
        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(1L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 20));
        req.setFechaSalida(LocalDate.of(2026, 11, 23));
        req.setNumHuespedes(2);

        when(reservaDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> reservaService.update(99L, req));
    }

    @Test
    void updateExitoso_cambiarEstadoACancelada() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Huesped hu = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        Reserva reservaExistente = new Reserva(1L, h, hu, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);

        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(1L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 20));
        req.setFechaSalida(LocalDate.of(2026, 11, 23));
        req.setNumHuespedes(2);
        req.setEstado(EstadoReserva.CANCELADA);

        Reserva saved = new Reserva(1L, h, hu, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CANCELADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);
        ReservaResponse resp = new ReservaResponse(1L, 1L, 1L, "101", "Juan Perez", "12345678", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 3, 2, EstadoReserva.CANCELADA, new BigDecimal("240000"), BigDecimal.ZERO, java.util.Map.of());

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservaDao.findById(1L)).thenReturn(Optional.of(reservaExistente));
        when(habitacionDao.findById(1L)).thenReturn(Optional.of(h));
        when(huespedDao.findById(1L)).thenReturn(Optional.of(hu));
        when(reservaDao.save(reservaExistente)).thenReturn(saved);
        when(reservaMapper.toResponse(saved)).thenReturn(resp);

        ReservaResponse result = reservaService.update(1L, req);

        assertNotNull(result);
        assertEquals(EstadoReserva.CANCELADA, result.estado());
    }

    @Test
    void updateFallo_habitacionNoEncontrada() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Huesped hu = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        Reserva reservaExistente = new Reserva(1L, h, hu, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);

        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(99L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 20));
        req.setFechaSalida(LocalDate.of(2026, 11, 23));
        req.setNumHuespedes(2);

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservaDao.findById(1L)).thenReturn(Optional.of(reservaExistente));
        when(habitacionDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> reservaService.update(1L, req));
    }

    @Test
    void updateFallo_habitacionMantenimiento() {
        Habitacion hActual = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Habitacion hNueva = new HabitacionSencilla(2L, "102", 2, new BigDecimal("80000"), EstadoHabitacion.MANTENIMIENTO);
        Huesped hu = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        Reserva reservaExistente = new Reserva(1L, hActual, hu, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);

        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(2L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 20));
        req.setFechaSalida(LocalDate.of(2026, 11, 23));
        req.setNumHuespedes(2);

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservaDao.findById(1L)).thenReturn(Optional.of(reservaExistente));
        when(habitacionDao.findById(2L)).thenReturn(Optional.of(hNueva));

        assertThrows(HabitacionNoDisponibleException.class, () -> reservaService.update(1L, req));
    }

    @Test
    void updateFallo_huespedNoEncontrado() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Reserva reservaExistente = new Reserva(1L, h, new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null), LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);

        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(1L);
        req.setHuespedId(99L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 20));
        req.setFechaSalida(LocalDate.of(2026, 11, 23));
        req.setNumHuespedes(2);

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservaDao.findById(1L)).thenReturn(Optional.of(reservaExistente));
        when(habitacionDao.findById(1L)).thenReturn(Optional.of(h));
        when(huespedDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> reservaService.update(1L, req));
    }

    @Test
    void updateFallo_fechaEntradaPasada() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Huesped hu = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        Reserva reservaExistente = new Reserva(1L, h, hu, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);

        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(1L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 10));
        req.setFechaSalida(LocalDate.of(2026, 11, 23));
        req.setNumHuespedes(2);

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservaDao.findById(1L)).thenReturn(Optional.of(reservaExistente));
        when(habitacionDao.findById(1L)).thenReturn(Optional.of(h));
        when(huespedDao.findById(1L)).thenReturn(Optional.of(hu));

        assertThrows(IllegalArgumentException.class, () -> reservaService.update(1L, req));
    }

    @Test
    void updateFallo_fechaSalidaAntesDeEntrada() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Huesped hu = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        Reserva reservaExistente = new Reserva(1L, h, hu, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);

        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(1L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 25));
        req.setFechaSalida(LocalDate.of(2026, 11, 23));
        req.setNumHuespedes(2);

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservaDao.findById(1L)).thenReturn(Optional.of(reservaExistente));
        when(habitacionDao.findById(1L)).thenReturn(Optional.of(h));
        when(huespedDao.findById(1L)).thenReturn(Optional.of(hu));

        assertThrows(IllegalArgumentException.class, () -> reservaService.update(1L, req));
    }

    @Test
    void updateFallo_huespedesExcedenCapacidad() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Huesped hu = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        Reserva reservaExistente = new Reserva(1L, h, hu, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);

        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(1L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 20));
        req.setFechaSalida(LocalDate.of(2026, 11, 23));
        req.setNumHuespedes(10);

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservaDao.findById(1L)).thenReturn(Optional.of(reservaExistente));
        when(habitacionDao.findById(1L)).thenReturn(Optional.of(h));
        when(huespedDao.findById(1L)).thenReturn(Optional.of(hu));

        assertThrows(IllegalArgumentException.class, () -> reservaService.update(1L, req));
    }

    @Test
    void updateFallo_cruceFechas_nuevoDentroDeExistente() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Habitacion h2 = new HabitacionSencilla(2L, "102", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Huesped hu = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        Reserva reservaExistente = new Reserva(1L, h, hu, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);
        Reserva otraReserva = new Reserva(2L, h2, hu, LocalDate.of(2026, 11, 21), LocalDate.of(2026, 11, 22), 1, EstadoReserva.CONFIRMADA, new BigDecimal("80000"), BigDecimal.ZERO, null, null, null, null);

        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(2L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 21));
        req.setFechaSalida(LocalDate.of(2026, 11, 22));
        req.setNumHuespedes(1);

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservaDao.findById(1L)).thenReturn(Optional.of(reservaExistente));
        when(habitacionDao.findById(2L)).thenReturn(Optional.of(h2));
        when(huespedDao.findById(1L)).thenReturn(Optional.of(hu));
        when(reservaDao.existsOverlapExcluding(2L, 1L, LocalDate.of(2026, 11, 21), LocalDate.of(2026, 11, 22))).thenReturn(true);

        assertThrows(ReservaCruzadaException.class, () -> reservaService.update(1L, req));
    }

    @Test
    void updateExitoso_cambioHabitacionSinCruce() {
        Habitacion hActual = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Habitacion hNueva = new HabitacionSencilla(2L, "102", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Huesped hu = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        Reserva reservaExistente = new Reserva(1L, hActual, hu, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);

        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(2L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 20));
        req.setFechaSalida(LocalDate.of(2026, 11, 23));
        req.setNumHuespedes(2);

        Reserva saved = new Reserva(1L, hNueva, hu, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, null, null, null, null);
        ReservaResponse resp = new ReservaResponse(1L, 2L, 1L, "102", "Juan Perez", "12345678", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 3, 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, java.util.Map.of());

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservaDao.findById(1L)).thenReturn(Optional.of(reservaExistente));
        when(habitacionDao.findById(2L)).thenReturn(Optional.of(hNueva));
        when(huespedDao.findById(1L)).thenReturn(Optional.of(hu));
        when(reservaDao.existsOverlapExcluding(2L, 1L, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23))).thenReturn(false);
        when(cotizacionService.calculate(2L, 3)).thenReturn(new co.edu.ut.hotel.service.dto.response.CotizacionResponse(3, new BigDecimal("240000.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("240000.00")));
        when(reservaDao.save(reservaExistente)).thenReturn(saved);
        when(reservaMapper.toResponse(saved)).thenReturn(resp);

        ReservaResponse result = reservaService.update(1L, req);

        assertNotNull(result);
        assertEquals(2L, result.habitacionId());
        assertEquals("102", result.habitacionNumero());
    }
}
