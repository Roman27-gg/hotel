package co.edu.ut.hotel.service.exception;

/**
 * Se lanza cuando no se permite cancelar una reserva porque la estadia ya comenzo.
 */
public class CancelacionNoPermitidaException extends HotelException {

    public CancelacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }

}
