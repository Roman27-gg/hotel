package co.edu.ut.hotel.service.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de HuespedUtil.
 * Cubre validaciones de dominio de huespedes.
 */
class HuespedUtilTest {

    @Test
    void validarDocumentoValido() {
        HuespedUtil.validarDocumento("12345678");
        HuespedUtil.validarDocumento("ABC12345");
        HuespedUtil.validarDocumento("12345ABCDE");
    }

    @Test
    void validarDocumentoNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> HuespedUtil.validarDocumento(null));
    }

    @Test
    void validarDocumentoVacioLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> HuespedUtil.validarDocumento(""));
    }

    @Test
    void validarDocumentoSoloEspaciosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> HuespedUtil.validarDocumento("   "));
    }

    @Test
    void validarDocumentoMenosDeCincoCaracteresLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> HuespedUtil.validarDocumento("1234"));
    }

    @Test
    void validarDocumentoMasDeVeinteCaracteresLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> HuespedUtil.validarDocumento("123456789012345678901"));
    }

    @Test
    void validarDocumentoConEspaciosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> HuespedUtil.validarDocumento("1234 5678"));
    }

    @Test
    void validarDocumentoConSimbolosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> HuespedUtil.validarDocumento("1234-5678"));
    }

    @Test
    void validarDocumentoConPuntosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> HuespedUtil.validarDocumento("12.34.5678"));
    }
}
