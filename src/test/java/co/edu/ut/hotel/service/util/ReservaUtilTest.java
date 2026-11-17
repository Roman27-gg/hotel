package co.edu.ut.hotel.service.util;

import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.entity.HabitacionSencilla;
import co.edu.ut.hotel.service.entity.Huesped;
import co.edu.ut.hotel.service.entity.Reserva;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.EstadoReserva;
import co.edu.ut.hotel.service.exception.CancelacionNoPermitidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de ReservaUtil.
 * Cubre validaciones y cálculos de dominio de reservas.
 */
class ReservaUtilTest {

    @Test
    void validarFechasValidas() {
        ReservaUtil.validarFechas(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23));
    }

    @Test
    void validarFechasIgualesLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> ReservaUtil.validarFechas(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 20)));
    }

    @Test
    void validarFechasSalidaAntesLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> ReservaUtil.validarFechas(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 20)));
    }

    @Test
    void validarFechasNulaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> ReservaUtil.validarFechas(null, LocalDate.of(2026, 11, 23)));
        assertThrows(IllegalArgumentException.class,
                () -> ReservaUtil.validarFechas(LocalDate.of(2026, 11, 20), null));
    }

    @Test
    void validarCapacidadDentroRango() {
        ReservaUtil.validarCapacidad(1, 6);
        ReservaUtil.validarCapacidad(6, 6);
        ReservaUtil.validarCapacidad(2, 2);
    }

    @Test
    void validarCapacidadCeroLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> ReservaUtil.validarCapacidad(0, 6));
    }

    @Test
    void validarCapacidadExcedeLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> ReservaUtil.validarCapacidad(7, 6));
    }

    @Test
    void calcularPenalizacionDosODiasCero() {
        Habitacion habitacion = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Reserva reserva = new Reserva();
        reserva.setHabitacion(habitacion);
        reserva.setFechaEntrada(LocalDate.of(2026, 11, 20));
        reserva.setFechaSalida(LocalDate.of(2026, 11, 23));

        BigDecimal penalizacion = ReservaUtil.calcularPenalizacion(
                LocalDate.of(2026, 11, 17), reserva.getFechaEntrada(), habitacion.getTarifaNoche());
        assertEquals(BigDecimal.ZERO, penalizacion);
    }

    @Test
    void calcularPenalizacionUnDiaAntesUnaNoche() {
        Habitacion habitacion = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Reserva reserva = new Reserva();
        reserva.setHabitacion(habitacion);
        reserva.setFechaEntrada(LocalDate.of(2026, 11, 20));

        BigDecimal penalizacion = ReservaUtil.calcularPenalizacion(
                LocalDate.of(2026, 11, 19), reserva.getFechaEntrada(), habitacion.getTarifaNoche());
        assertEquals(new BigDecimal("80000"), penalizacion);
    }

    @Test
    void calcularPenalizacionMismoDiaUnaNoche() {
        Habitacion habitacion = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Reserva reserva = new Reserva();
        reserva.setHabitacion(habitacion);
        reserva.setFechaEntrada(LocalDate.of(2026, 11, 20));

        BigDecimal penalizacion = ReservaUtil.calcularPenalizacion(
                LocalDate.of(2026, 11, 20), reserva.getFechaEntrada(), habitacion.getTarifaNoche());
        assertEquals(new BigDecimal("80000"), penalizacion);
    }

    @Test
    void calcularPenalizacionDespuesEntradaLanzaExcepcion() {
        Habitacion habitacion = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Reserva reserva = new Reserva();
        reserva.setHabitacion(habitacion);
        reserva.setFechaEntrada(LocalDate.of(2026, 11, 10));
        reserva.setFechaSalida(LocalDate.of(2026, 11, 12));

        assertThrows(CancelacionNoPermitidaException.class,
                () -> ReservaUtil.calcularPenalizacion(
                        LocalDate.of(2026, 11, 11), reserva.getFechaEntrada(), habitacion.getTarifaNoche()));
    }

    @Test
    void checkInValido() {
        Habitacion habitacion = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Reserva reserva = new Reserva();
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setHabitacion(habitacion);

        ReservaUtil.checkIn(reserva);

        assertNotNull(reserva.getFechaCheckIn());
        assertEquals(EstadoHabitacion.OCUPADA, habitacion.getEstado());
    }

    @Test
    void checkInReservaNoConfirmadaLanzaExcepcion() {
        Reserva reserva = new Reserva();
        reserva.setEstado(EstadoReserva.CANCELADA);

        assertThrows(IllegalStateException.class, () -> ReservaUtil.checkIn(reserva));
    }

    @Test
    void checkOutValido() {
        Habitacion habitacion = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.OCUPADA);
        Reserva reserva = new Reserva();
        reserva.setFechaCheckIn(LocalDateTime.now());
        reserva.setHabitacion(habitacion);

        ReservaUtil.checkOut(reserva);

        assertNotNull(reserva.getFechaCheckOut());
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        assertEquals(EstadoHabitacion.DISPONIBLE, habitacion.getEstado());
    }

    @Test
    void checkOutSinCheckInLanzaExcepcion() {
        Reserva reserva = new Reserva();

        assertThrows(IllegalStateException.class, () -> ReservaUtil.checkOut(reserva));
    }
}
