package co.edu.ut.hotel.service.enums;

/**
 * Estado de una habitacion del hotel.
 * <ul>
 *   <li>DISPONIBLE: sin reservas activas vigentes o futuras.</li>
 *   <li>RESERVADA: tiene al menos una reserva CONFIRMADA vigente o futura.</li>
 *   <li>OCUPADA: un huesped esta alojado en la habitacion despues del check-in.</li>
 *   <li>MANTENIMIENTO: no se puede reservar.</li>
 * </ul>
 * El estado es un resumen para el tablero; la disponibilidad por fechas se decide
 * con la consulta de cruce de reservas, no solo con este campo.
 */
public enum EstadoHabitacion {
    DISPONIBLE,
    RESERVADA,
    OCUPADA,
    MANTENIMIENTO
}
