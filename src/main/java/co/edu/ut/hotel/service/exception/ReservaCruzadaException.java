package co.edu.ut.hotel.service.exception;

/**
 * Se lanza cuando dos reservas se cruzan en fechas para la misma habitacion.
 * Provoca el rollback de la transaccion de creacion de reserva.
 */
public class ReservaCruzadaException extends HotelException {

    public ReservaCruzadaException(String mensaje) {
        super(mensaje);
    }

}
