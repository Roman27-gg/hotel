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
        ReservaResponse resp = new ReservaResponse(1L, "101", "Juan Perez", "12345678", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 3, 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, java.util.Map.of());

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
        when(reservaMapper.toResponse(r)).thenReturn(new ReservaResponse(1L, "101", "Juan Perez", "12345678", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 3, 2, EstadoReserva.CONFIRMADA, new BigDecimal("240000"), BigDecimal.ZERO, java.util.Map.of()));

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
}
