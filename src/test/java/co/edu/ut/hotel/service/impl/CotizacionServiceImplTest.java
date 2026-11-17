package co.edu.ut.hotel.service.impl;

import co.edu.ut.hotel.service.CotizacionService;
import co.edu.ut.hotel.service.dto.response.CotizacionResponse;
import co.edu.ut.hotel.service.repository.HabitacionDao;
import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.entity.HabitacionSencilla;
import co.edu.ut.hotel.service.entity.HabitacionSuite;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import co.edu.ut.hotel.service.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de CotizacionServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CotizacionServiceImplTest {

    @Mock
    private HabitacionDao habitacionDao;

    @InjectMocks
    private CotizacionServiceImpl cotizacionService;

    private static BigDecimal bd(String val) {
        return new BigDecimal(val);
    }

    @Test
    void calculateSencillaUnaNoche() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        when(habitacionDao.findById(1L)).thenReturn(Optional.of(h));

        CotizacionResponse response = cotizacionService.calculate(1L, 1);

        assertEquals(1, response.noches());
        assertTrue(response.subtotal().compareTo(bd("80000.00")) == 0);
        assertTrue(response.recargo().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(response.descuento().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(response.total().compareTo(bd("80000.00")) == 0);
    }

    @Test
    void calculateSencillaTresNoches() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        when(habitacionDao.findById(1L)).thenReturn(Optional.of(h));

        CotizacionResponse response = cotizacionService.calculate(1L, 3);

        assertEquals(3, response.noches());
        assertTrue(response.subtotal().compareTo(bd("240000.00")) == 0);
        assertTrue(response.recargo().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(response.descuento().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(response.total().compareTo(bd("240000.00")) == 0);
    }

    @Test
    void calculateSuiteUnaNoche() {
        Habitacion h = new HabitacionSuite(2L, "201", 4, new BigDecimal("200000"), EstadoHabitacion.DISPONIBLE);
        when(habitacionDao.findById(2L)).thenReturn(Optional.of(h));

        CotizacionResponse response = cotizacionService.calculate(2L, 1);

        assertEquals(1, response.noches());
        assertTrue(response.subtotal().compareTo(bd("200000.00")) == 0);
        assertTrue(response.recargo().compareTo(bd("30000.00")) == 0);
        assertTrue(response.descuento().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(response.total().compareTo(bd("230000.00")) == 0);
    }

    @Test
    void calculateSuiteCincoNochesConDescuento() {
        Habitacion h = new HabitacionSuite(2L, "201", 4, new BigDecimal("200000"), EstadoHabitacion.DISPONIBLE);
        when(habitacionDao.findById(2L)).thenReturn(Optional.of(h));

        CotizacionResponse response = cotizacionService.calculate(2L, 5);

        assertEquals(5, response.noches());
        assertTrue(response.subtotal().compareTo(bd("1000000.00")) == 0);
        assertTrue(response.recargo().compareTo(bd("150000.00")) == 0);
        assertTrue(response.descuento().compareTo(bd("115000.00")) == 0);
        assertTrue(response.total().compareTo(bd("1035000.00")) == 0);
    }

    @Test
    void calculateHabitacionNoEncontrada() {
        when(habitacionDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> cotizacionService.calculate(99L, 1));
    }

    @Test
    void calculateHabitacionDaoVerificado() {
        Habitacion h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        when(habitacionDao.findById(1L)).thenReturn(Optional.of(h));

        cotizacionService.calculate(1L, 1);

        verify(habitacionDao, times(1)).findById(1L);
    }
}
