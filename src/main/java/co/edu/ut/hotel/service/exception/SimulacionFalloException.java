package co.edu.ut.hotel.service.exception;

/**
 * Se lanza solo en el perfil demo cuando el usuario marca "simular fallo" despues de guardar.
 * Permite demostrar que el rollback deshace tanto la insercion de la reserva como
 * la actualizacion del estado de la habitacion.
 */
public class SimulacionFalloException extends HotelException {

    public SimulacionFalloException(String mensaje) {
        super(mensaje);
    }

}
