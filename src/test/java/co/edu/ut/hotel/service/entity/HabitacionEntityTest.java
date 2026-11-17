package co.edu.ut.hotel.service.entity;

import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import co.edu.ut.hotel.service.exception.CancelacionNoPermitidaException;
import co.edu.ut.hotel.service.util.HabitacionUtil;
import co.edu.ut.hotel.service.util.ReservaUtil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de las utilidades de dominio (sin base de datos).
 * Las entidades son puras (solo datos), las reglas están en las utilidades.
 */
class HabitacionEntityTest {

    private static final EstadoHabitacion DISPONIBLE = EstadoHabitacion.DISPONIBLE;

    @Test
    void sencillaCalculaValorSinRecargoNiDescuento() {
        HabitacionSencilla h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), DISPONIBLE);
        assertEquals(TipoHabitacion.SENCILLA, h.getTipo());
        assertEquals(0, HabitacionUtil.calcularValorEstadiaSencilla(h.getTarifaNoche(), 1).compareTo(new BigDecimal("80000")));
        assertEquals(0, HabitacionUtil.calcularValorEstadiaSencilla(h.getTarifaNoche(), 3).compareTo(new BigDecimal("240000")));
        assertTrue(HabitacionUtil.admite(2, h.getCapacidad()));
        assertFalse(HabitacionUtil.admite(3, h.getCapacidad()));
        assertFalse(HabitacionUtil.estaEnMantenimiento(h.getEstado()));
    }

    @Test
    void suiteAplicaRecargoYDescuento() {
        HabitacionSuite h = new HabitacionSuite(2L, "201", 4, new BigDecimal("200000"), DISPONIBLE);
        assertEquals(TipoHabitacion.SUITE, h.getTipo());
        assertEquals(0, HabitacionUtil.calcularValorEstadiaSuite(h.getTarifaNoche(), 1).compareTo(new BigDecimal("230000")));
        assertEquals(0, HabitacionUtil.calcularValorEstadiaSuite(h.getTarifaNoche(), 4).compareTo(new BigDecimal("920000")));
        assertEquals(0, HabitacionUtil.calcularValorEstadiaSuite(h.getTarifaNoche(), 5).compareTo(new BigDecimal("1035000")));
    }

    @Test
    void suiteDesgloseIncluyeRecargoYDescuento() {
        HabitacionSuite h = new HabitacionSuite(2L, "201", 4, new BigDecimal("200000"), DISPONIBLE);
        Map<String, BigDecimal> d = HabitacionUtil.desglosarSuite(h.getTarifaNoche(), 5);
        assertEquals(0, d.get("Total").compareTo(new BigDecimal("1035000")));
        assertNotNull(d.get("Recargo servicios"));
        assertNotNull(d.get("Descuento estadia larga"));
    }

    @Test
    void sencillaDesgloseSoloSubtotalYTotal() {
        HabitacionSencilla h = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), DISPONIBLE);
        Map<String, BigDecimal> d = HabitacionUtil.desglosarSencilla(h.getTarifaNoche(), 3);
        assertEquals(0, d.get("Total").compareTo(new BigDecimal("240000")));
        assertEquals(2, d.size());
    }

    @Test
    void reservaNochesYPenalizacion() {
        HabitacionSencilla habitacion = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), DISPONIBLE);
        co.edu.ut.hotel.service.entity.Reserva r = new co.edu.ut.hotel.service.entity.Reserva();
        r.setHabitacion(habitacion);
        r.setFechaEntrada(LocalDate.of(2026, 11, 20));
        r.setFechaSalida(LocalDate.of(2026, 11, 23));

        int noches = HabitacionUtil.calcularNoches(r.getFechaEntrada(), r.getFechaSalida());
        assertEquals(3, noches);

        // 2 o mas dias antes: penalizacion 0
        assertEquals(0, ReservaUtil.calcularPenalizacion(LocalDate.of(2026, 11, 17), r.getFechaEntrada(), habitacion.getTarifaNoche()).compareTo(BigDecimal.ZERO));
        // 1 dia antes: 1 noche
        assertEquals(0, ReservaUtil.calcularPenalizacion(LocalDate.of(2026, 11, 19), r.getFechaEntrada(), habitacion.getTarifaNoche()).compareTo(new BigDecimal("80000")));
        // mismo dia (inclusive): 1 noche
        assertEquals(0, ReservaUtil.calcularPenalizacion(LocalDate.of(2026, 11, 20), r.getFechaEntrada(), habitacion.getTarifaNoche()).compareTo(new BigDecimal("80000")));
    }

    @Test
    void reservaNoPermiteCancelarDespuesDeEntrada() {
        HabitacionSencilla habitacion = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), DISPONIBLE);
        co.edu.ut.hotel.service.entity.Reserva r = new co.edu.ut.hotel.service.entity.Reserva();
        r.setHabitacion(habitacion);
        r.setFechaEntrada(LocalDate.of(2026, 11, 10));
        r.setFechaSalida(LocalDate.of(2026, 11, 12));
        assertThrows(CancelacionNoPermitidaException.class,
                () -> ReservaUtil.calcularPenalizacion(LocalDate.of(2026, 11, 11), r.getFechaEntrada(), habitacion.getTarifaNoche()));
    }

    @Test
    void habitacionUtilRechazaTarifaNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> HabitacionUtil.validarTarifa(new BigDecimal("-100")));
    }

    @Test
    void habitacionUtilRechazaCapacidadFueraDeRango() {
        assertThrows(IllegalArgumentException.class,
                () -> HabitacionUtil.validarCapacidad(0));
    }

    @Test
    void reservaUtilCalcularPenalizacionDespuesDeEntrada() {
        HabitacionSencilla habitacion = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), DISPONIBLE);
        assertThrows(CancelacionNoPermitidaException.class,
                () -> ReservaUtil.calcularPenalizacion(
                        LocalDate.of(2026, 11, 11),
                        LocalDate.of(2026, 11, 10),
                        habitacion.getTarifaNoche()
                ));
    }
}
