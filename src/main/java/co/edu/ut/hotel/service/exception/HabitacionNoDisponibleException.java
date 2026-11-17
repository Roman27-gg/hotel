package co.edu.ut.hotel.service.exception;

/**
 * Se lanza cuando la habitacion esta en mantenimiento o no esta disponible.
 */
public class HabitacionNoDisponibleException extends HotelException {

    public HabitacionNoDisponibleException(String mensaje) {
        super(mensaje);
    }

}
