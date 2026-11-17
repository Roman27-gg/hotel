package co.edu.ut.hotel.service.impl;

import co.edu.ut.hotel.service.HabitacionService;
import co.edu.ut.hotel.service.dto.request.HabitacionRequest;
import co.edu.ut.hotel.service.dto.response.HabitacionResponse;
import co.edu.ut.hotel.service.repository.HabitacionDao;
import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.entity.HabitacionSencilla;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import co.edu.ut.hotel.service.exception.HabitacionConReservasException;
import co.edu.ut.hotel.service.exception.RecursoNoEncontradoException;
import co.edu.ut.hotel.service.mapper.HabitacionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de HabitacionServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class HabitacionServiceImplTest {

    @Mock
    private HabitacionDao habitacionDao;

    @Mock
    private HabitacionMapper habitacionMapper;

    @InjectMocks
    private HabitacionServiceImpl habitacionService;

    @Test
    void findAllDevuelveLista() {
        when(habitacionDao.findAll()).thenReturn(List.of());
        when(habitacionMapper.toResponseList(any())).thenReturn(List.of());

        List<HabitacionResponse> result = habitacionService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdEncontrado() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        when(habitacionDao.findById(1L)).thenReturn(Optional.of(h));
        when(habitacionMapper.toResponse(h)).thenReturn(new HabitacionResponse(1L, "101", TipoHabitacion.SENCILLA, 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE));

        HabitacionResponse result = habitacionService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("101", result.numero());
    }

    @Test
    void findByIdNoEncontrado() {
        when(habitacionDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> habitacionService.findById(99L));
    }

    @Test
    void createExitoso() {
        HabitacionRequest req = new HabitacionRequest();
        req.setNumero("101");
        req.setTipo("SENCILLA");
        req.setCapacidad(2);
        req.setTarifaNoche(new BigDecimal("80000"));
        req.setEstado("DISPONIBLE");

        Habitacion entity = new HabitacionSencilla(null, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Habitacion saved = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        HabitacionResponse resp = new HabitacionResponse(1L, "101", TipoHabitacion.SENCILLA, 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);

        when(habitacionDao.findByNumero("101")).thenReturn(Optional.empty());
        when(habitacionMapper.toEntity(req)).thenReturn(entity);
        when(habitacionDao.save(entity)).thenReturn(saved);
        when(habitacionMapper.toResponse(saved)).thenReturn(resp);

        HabitacionResponse result = habitacionService.create(req);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void createNumeroDuplicado() {
        HabitacionRequest req = new HabitacionRequest();
        req.setNumero("101");
        req.setTipo("SENCILLA");
        req.setCapacidad(2);
        req.setTarifaNoche(new BigDecimal("80000"));
        req.setEstado("DISPONIBLE");

        Habitacion existente = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        when(habitacionDao.findByNumero("101")).thenReturn(Optional.of(existente));

        assertThrows(IllegalArgumentException.class, () -> habitacionService.create(req));
    }

    @Test
    void deleteSinReservas() {
        when(habitacionDao.findById(1L)).thenReturn(Optional.of(new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE)));
        when(habitacionDao.countReservasByRoom(1L)).thenReturn(0);

        habitacionService.delete(1L);

        verify(habitacionDao, times(1)).deleteById(1L);
    }

    @Test
    void deleteConReservasLanzaExcepcion() {
        when(habitacionDao.findById(1L)).thenReturn(Optional.of(new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE)));
        when(habitacionDao.countReservasByRoom(1L)).thenReturn(2);

        assertThrows(HabitacionConReservasException.class, () -> habitacionService.delete(1L));
    }

    @Test
    void findByTypeAndState() {
        when(habitacionDao.findByTypeAndState(TipoHabitacion.SENCILLA, EstadoHabitacion.DISPONIBLE))
                .thenReturn(List.of());
        when(habitacionMapper.toResponseList(any())).thenReturn(List.of());

        List<HabitacionResponse> result = habitacionService.findByTypeAndState("SENCILLA", "DISPONIBLE");

        assertNotNull(result);
    }
}
