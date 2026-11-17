package co.edu.ut.hotel.service.exception;

/**
 * Se lanza cuando se intenta eliminar una habitacion que tiene reservas asociadas.
 */
public class HabitacionConReservasException extends HotelException {

    public HabitacionConReservasException(String mensaje) {
        super(mensaje);
    }

}
