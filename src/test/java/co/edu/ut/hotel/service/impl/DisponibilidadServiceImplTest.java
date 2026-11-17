package co.edu.ut.hotel.service.impl;

import co.edu.ut.hotel.service.CotizacionService;
import co.edu.ut.hotel.service.DisponibilidadService;
import co.edu.ut.hotel.service.dto.response.CotizacionResponse;
import co.edu.ut.hotel.service.dto.response.HabitacionDisponibleResponse;
import co.edu.ut.hotel.service.repository.HabitacionDao;
import co.edu.ut.hotel.service.repository.ReservaDao;
import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.entity.HabitacionSencilla;
import co.edu.ut.hotel.service.entity.HabitacionSuite;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de DisponibilidadServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DisponibilidadServiceImplTest {

    @Mock
    private HabitacionDao habitacionDao;

    @Mock
    private ReservaDao reservaDao;

    @Mock
    private CotizacionService cotizacionService;

    @InjectMocks
    private DisponibilidadServiceImpl disponibilidadService;

    @Test
    void findAvailableSinHabitaciones() {
        when(habitacionDao.findAll()).thenReturn(List.of());

        List<HabitacionDisponibleResponse> result = disponibilidadService.findAvailable(
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAvailableConHabitacionMantenimiento() {
        Habitacion m = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.MANTENIMIENTO);
        Habitacion d = new HabitacionSencilla(2L, "102", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);

        when(habitacionDao.findAll()).thenReturn(List.of(m, d));
        when(reservaDao.existsOverlap(2L, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23))).thenReturn(false);
        when(cotizacionService.calculate(2L, 3)).thenReturn(new CotizacionResponse(3,
                new BigDecimal("240000.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("240000.00")));

        List<HabitacionDisponibleResponse> result = disponibilidadService.findAvailable(
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), null, null);

        assertEquals(1, result.size());
        assertEquals("102", result.get(0).numero());
    }

    @Test
    void findAvailableConCruceFechas() {
        Habitacion d = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);

        when(habitacionDao.findAll()).thenReturn(List.of(d));
        when(reservaDao.existsOverlap(1L, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23))).thenReturn(true);

        List<HabitacionDisponibleResponse> result = disponibilidadService.findAvailable(
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAvailableFiltroTipo() {
        Habitacion s = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Habitacion su = new HabitacionSuite(2L, "201", 4, new BigDecimal("200000"), EstadoHabitacion.DISPONIBLE);

        when(habitacionDao.findAll()).thenReturn(List.of(s, su));
        when(reservaDao.existsOverlap(2L, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23))).thenReturn(false);
        when(cotizacionService.calculate(2L, 3)).thenReturn(new CotizacionResponse(3,
                new BigDecimal("1035000.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("1035000.00")));

        List<HabitacionDisponibleResponse> result = disponibilidadService.findAvailable(
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), "SUITE", null);

        assertEquals(1, result.size());
        assertEquals("201", result.get(0).numero());
    }

    @Test
    void findAvailableFiltroPersonas() {
        Habitacion s = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);

        when(habitacionDao.findAll()).thenReturn(List.of(s));
        when(reservaDao.existsOverlap(1L, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23))).thenReturn(false);
        when(cotizacionService.calculate(1L, 3)).thenReturn(new CotizacionResponse(3,
                new BigDecimal("240000.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("240000.00")));

        List<HabitacionDisponibleResponse> result = disponibilidadService.findAvailable(
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), null, 3);

        assertTrue(result.isEmpty());
    }

    @Test
    void quoteValido() {
        when(cotizacionService.calculate(1L, 3))
                .thenReturn(new CotizacionResponse(3, new BigDecimal("240000.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("240000.00")));

        CotizacionResponse response = disponibilidadService.quote(1L, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));

        assertNotNull(response);
        assertEquals(new BigDecimal("240000.00"), response.total());
    }
}
