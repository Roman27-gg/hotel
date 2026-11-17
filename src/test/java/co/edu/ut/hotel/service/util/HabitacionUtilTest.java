package co.edu.ut.hotel.service.util;

import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de HabitacionUtil.
 * Cubre validaciones y cálculos de dominio de habitaciones.
 */
class HabitacionUtilTest {

    @Test
    void validarTarifaValidaNoLanzaExcepcion() {
        HabitacionUtil.validarTarifa(new BigDecimal("0.01"));
        HabitacionUtil.validarTarifa(new BigDecimal("100000.00"));
    }

    @Test
    void validarTarifaNulaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> HabitacionUtil.validarTarifa(null));
    }

    @Test
    void validarTarifaNegativaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> HabitacionUtil.validarTarifa(new BigDecimal("-1")));
    }

    @Test
    void validarCapacidadRangoValido() {
        HabitacionUtil.validarCapacidad(1);
        HabitacionUtil.validarCapacidad(6);
    }

    @Test
    void validarCapacidadCeroLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> HabitacionUtil.validarCapacidad(0));
    }

    @Test
    void validarCapacidadSieteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> HabitacionUtil.validarCapacidad(7));
    }

    @Test
    void admiteCapacidadValida() {
        assertTrue(HabitacionUtil.admite(1, 6));
        assertTrue(HabitacionUtil.admite(6, 6));
        assertTrue(HabitacionUtil.admite(3, 3));
    }

    @Test
    void admiteCapacidadInvalida() {
        assertFalse(HabitacionUtil.admite(0, 6));
        assertFalse(HabitacionUtil.admite(7, 6));
        assertFalse(HabitacionUtil.admite(2, 1));
    }

    @Test
    void calcularNochesIguales() {
        LocalDate entrada = LocalDate.of(2026, 11, 20);
        LocalDate salida = LocalDate.of(2026, 11, 23);
        assertEquals(3, HabitacionUtil.calcularNoches(entrada, salida));
    }

    @Test
    void calcularNochesUnaNoche() {
        LocalDate entrada = LocalDate.of(2026, 11, 20);
        LocalDate salida = LocalDate.of(2026, 11, 21);
        assertEquals(1, HabitacionUtil.calcularNoches(entrada, salida));
    }

    @Test
    void redondearDosDecimales() {
        assertEquals(new BigDecimal("100.00"), HabitacionUtil.redondear(new BigDecimal("100.001")));
        assertEquals(new BigDecimal("100.01"), HabitacionUtil.redondear(new BigDecimal("100.005")));
        assertEquals(new BigDecimal("100.00"), HabitacionUtil.redondear(new BigDecimal("100.004")));
    }

    @Test
    void calcularValorEstadiaSencilla() {
        BigDecimal tarifa = new BigDecimal("80000");
        assertEquals(new BigDecimal("80000.00"), HabitacionUtil.calcularValorEstadiaSencilla(tarifa, 1));
        assertEquals(new BigDecimal("240000.00"), HabitacionUtil.calcularValorEstadiaSencilla(tarifa, 3));
    }

    @Test
    void calcularValorEstadiaSuiteUnaNoche() {
        BigDecimal tarifa = new BigDecimal("200000");
        // 200000 + 15% = 230000
        assertEquals(new BigDecimal("230000.00"), HabitacionUtil.calcularValorEstadiaSuite(tarifa, 1));
    }

    @Test
    void calcularValorEstadiaSuiteCuatroNochesSinDescuento() {
        BigDecimal tarifa = new BigDecimal("200000");
        // 200000 * 4 = 800000 + 15% = 920000
        assertEquals(new BigDecimal("920000.00"), HabitacionUtil.calcularValorEstadiaSuite(tarifa, 4));
    }

    @Test
    void calcularValorEstadiaSuiteCincoNochesConDescuento() {
        BigDecimal tarifa = new BigDecimal("200000");
        // 200000 * 5 = 1000000 + 15% = 1150000 - 10% = 1035000
        assertEquals(new BigDecimal("1035000.00"), HabitacionUtil.calcularValorEstadiaSuite(tarifa, 5));
    }

    @Test
    void desglosarSencilla() {
        Map<String, BigDecimal> d = HabitacionUtil.desglosarSencilla(new BigDecimal("80000"), 3);
        assertEquals(2, d.size());
        assertEquals(new BigDecimal("240000.00"), d.get("Subtotal"));
        assertEquals(new BigDecimal("240000.00"), d.get("Total"));
    }

    @Test
    void desglosarSuite() {
        Map<String, BigDecimal> d = HabitacionUtil.desglosarSuite(new BigDecimal("200000"), 5);
        assertEquals(4, d.size());
        assertEquals(new BigDecimal("1000000.00"), d.get("Subtotal"));
        assertEquals(new BigDecimal("150000.00"), d.get("Recargo servicios"));
        assertEquals(new BigDecimal("115000.00"), d.get("Descuento estadia larga"));
        assertEquals(new BigDecimal("1035000.00"), d.get("Total"));
    }

    @Test
    void estaEnMantenimientoTrue() {
        assertTrue(HabitacionUtil.estaEnMantenimiento(EstadoHabitacion.MANTENIMIENTO));
    }

    @Test
    void estaEnMantenimientoFalse() {
        assertFalse(HabitacionUtil.estaEnMantenimiento(EstadoHabitacion.DISPONIBLE));
        assertFalse(HabitacionUtil.estaEnMantenimiento(EstadoHabitacion.RESERVADA));
        assertFalse(HabitacionUtil.estaEnMantenimiento(EstadoHabitacion.OCUPADA));
    }
}
