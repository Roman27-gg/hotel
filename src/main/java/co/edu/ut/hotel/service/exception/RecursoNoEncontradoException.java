package co.edu.ut.hotel.service.exception;

/**
 * Se lanza cuando no se encuentra un recurso (habitacion, huesped o reserva) por su identificador.
 */
public class RecursoNoEncontradoException extends HotelException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

}
