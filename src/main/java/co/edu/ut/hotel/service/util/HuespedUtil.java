package co.edu.ut.hotel.service.util;

/**
 * Utilidad con las validaciones de dominio de los huéspedes.
 */
public final class HuespedUtil {

    private HuespedUtil() {
    }

    /**
     * Valida el documento de un huésped.
     * Debe contener solo letras y dígitos, con longitud entre 5 y 20.
     *
     * @param documento documento a validar
     * @throws IllegalArgumentException si el documento es inválido
     */
    public static void validarDocumento(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }
        if (!documento.matches("^[A-Za-z0-9]+$")) {
            throw new IllegalArgumentException("El documento debe contener solo letras y digitos");
        }
        if (documento.length() < 5 || documento.length() > 20) {
            throw new IllegalArgumentException("El documento debe tener entre 5 y 20 caracteres");
        }
    }
}
