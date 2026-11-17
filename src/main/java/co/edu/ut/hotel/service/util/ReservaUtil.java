package co.edu.ut.hotel.service.util;

import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.entity.Reserva;
import co.edu.ut.hotel.service.exception.CancelacionNoPermitidaException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utilidad con las validaciones y cálculos de dominio de las reservas.
 */
public final class ReservaUtil {

    private ReservaUtil() {
    }

    /**
     * Valida que las fechas de entrada y salida sean válidas.
     *
     * @param fechaEntrada fecha de entrada
     * @param fechaSalida  fecha de salida
     * @throws IllegalArgumentException si las fechas son nulas o la salida no es posterior a la entrada
     */
    public static void validarFechas(LocalDate fechaEntrada, LocalDate fechaSalida) {
        if (fechaEntrada == null || fechaSalida == null) {
            throw new IllegalArgumentException("Las fechas de entrada y salida son obligatorias");
        }
        if (!fechaSalida.isAfter(fechaEntrada)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada");
        }
    }

    /**
     * Valida que el número de huéspedes esté entre 1 y la capacidad de la habitación.
     *
     * @param numHuespedes número de huéspedes
     * @param capacidad    capacidad de la habitación
     * @throws IllegalArgumentException si el número de huéspedes es inválido
     */
    public static void validarCapacidad(int numHuespedes, int capacidad) {
        if (numHuespedes < 1 || numHuespedes > capacidad) {
            throw new IllegalArgumentException("El número de huéspedes debe estar entre 1 y la capacidad de la habitación");
        }
    }

    /**
     * Calcula la penalización por cancelación según la fecha de hoy.
     *
     * @param hoy          fecha actual (obtenida del reloj inyectado)
     * @param fechaEntrada fecha de entrada de la reserva
     * @param tarifaNoche  tarifa por noche de la habitación
     * @return monto de la penalización
     * @throws CancelacionNoPermitidaException si la estadía ya comenzó
     */
    public static BigDecimal calcularPenalizacion(LocalDate hoy, LocalDate fechaEntrada, BigDecimal tarifaNoche) {
        if (hoy == null) {
            hoy = LocalDate.now(Clock.systemDefaultZone());
        }
        if (hoy.isAfter(fechaEntrada)) {
            throw new CancelacionNoPermitidaException("No se puede cancelar una reserva cuya estadía ya comenzó");
        }
        long diasHastaEntrada = java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaEntrada);
        if (diasHastaEntrada >= 2) {
            return BigDecimal.ZERO;
        }
        // menos de 2 días o mismo día (inclusive): 1 noche de tarifa
        return tarifaNoche.multiply(new BigDecimal(1));
    }

    /**
     * Realiza el check-in de la reserva.
     * La reserva se mantiene CONFIRMADA, se registra la fecha de check-in y la habitación pasa a OCUPADA.
     *
     * @param reserva reserva a registrar check-in
     * @throws IllegalStateException si la reserva no está CONFIRMADA
     */
    public static void checkIn(Reserva reserva) {
        if (reserva.getEstado() != co.edu.ut.hotel.service.enums.EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo se puede hacer check-in a una reserva CONFIRMADA");
        }
        reserva.setFechaCheckIn(LocalDateTime.now(Clock.systemDefaultZone()));
        if (reserva.getHabitacion() != null) {
            reserva.getHabitacion().setEstado(co.edu.ut.hotel.service.enums.EstadoHabitacion.OCUPADA);
        }
    }

    /**
     * Realiza el check-out de la reserva.
     * La reserva pasa a FINALIZADA, se registra la fecha de check-out y la habitación queda disponible de nuevo.
     *
     * @param reserva reserva a registrar check-out
     * @throws IllegalStateException si la reserva no tiene check-in previo
     */
    public static void checkOut(Reserva reserva) {
        if (reserva.getFechaCheckIn() == null) {
            throw new IllegalStateException("Solo se puede hacer check-out después del check-in");
        }
        reserva.setEstado(co.edu.ut.hotel.service.enums.EstadoReserva.FINALIZADA);
        reserva.setFechaCheckOut(LocalDateTime.now(Clock.systemDefaultZone()));
        if (reserva.getHabitacion() != null) {
            reserva.getHabitacion().setEstado(co.edu.ut.hotel.service.enums.EstadoHabitacion.DISPONIBLE);
        }
    }
}
