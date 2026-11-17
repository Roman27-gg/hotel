package co.edu.ut.hotel.service.exception;

/**
 * Excepcion base de dominio del hotel.
 * Todas las excepciones de negocio extienden esta clase (RuntimeException)
 * para que Spring realice el rollback automatico de la transaccion.
 */
public class HotelException extends RuntimeException {

    public HotelException(String mensaje) {
        super(mensaje);
    }

}
