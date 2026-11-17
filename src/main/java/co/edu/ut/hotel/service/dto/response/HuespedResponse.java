package co.edu.ut.hotel.service.dto.response;

/**
 * DTO de salida con los datos de un huesped listos para mostrar en la vista.
 */
public record HuespedResponse(
        Long id,
        String documento,
        String nombre,
        String apellido,
        String telefono,
        String email
) {

    /**
     * Devuelve el nombre completo del huesped.
     *
     * @return nombre y apellido concatenados
     */
    public String getNombreCompleto() {
        return (nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido);
    }
}
