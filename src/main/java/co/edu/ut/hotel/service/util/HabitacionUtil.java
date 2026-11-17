package co.edu.ut.hotel.service.util;

import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utilidad con las validaciones y cálculos de dominio de las habitaciones.
 * Centraliza las reglas de negocio para evitar duplicación entre clases hijas.
 */
public final class HabitacionUtil {

    private HabitacionUtil() {
    }

    /**
     * Valida que la tarifa por noche sea mayor a cero.
     *
     * @param tarifaNoche tarifa a validar
     * @throws IllegalArgumentException si la tarifa es nula o no es positiva
     */
    public static void validarTarifa(BigDecimal tarifaNoche) {
        if (tarifaNoche == null || tarifaNoche.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La tarifa por noche debe ser mayor a cero");
        }
    }

    /**
     * Valida que la capacidad esté entre 1 y 6.
     *
     * @param capacidad capacidad a validar
     * @throws IllegalArgumentException si la capacidad está fuera de rango
     */
    public static void validarCapacidad(int capacidad) {
        if (capacidad < 1 || capacidad > 6) {
            throw new IllegalArgumentException("La capacidad debe estar entre 1 y 6");
        }
    }

    /**
     * Indica si la habitación admite la cantidad de personas dada.
     *
     * @param personas  número de personas
     * @param capacidad capacidad de la habitación
     * @return true si personas está entre 1 y la capacidad
     */
    public static boolean admite(int personas, int capacidad) {
        return personas >= 1 && personas <= capacidad;
    }

    /**
     * Calcula el número de noches como valor entero redondeado hacia arriba.
     * Se usa para fines de display cuando se reciben fechas como LocalDate.
     *
     * @param fechaEntrada fecha de entrada
     * @param fechaSalida  fecha de salida
     * @return número de noches (mínimo 1)
     */
    public static int calcularNoches(LocalDate fechaEntrada, LocalDate fechaSalida) {
        int noches = (int) java.time.temporal.ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
        return Math.max(noches, 1);
    }

    /**
     * Redondea un monto a dos decimales con HALF_UP.
     *
     * @param valor monto a redondear
     * @return valor redondeado
     */
    public static BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el valor de estadía para una habitación sencilla (tarifa × noches).
     *
     * @param tarifaNoche tarifa por noche
     * @param noches      número de noches
     * @return valor total de la estadía
     */
    public static BigDecimal calcularValorEstadiaSencilla(BigDecimal tarifaNoche, int noches) {
        return redondear(tarifaNoche.multiply(new BigDecimal(noches)));
    }

    /**
     * Calcula el valor de estadía para una habitación suite:
     * tarifa × noches + 15% recargo de servicios, con 10% de descuento desde 5 noches.
     *
     * @param tarifaNoche tarifa por noche
     * @param noches      número de noches
     * @return valor total de la estadía
     */
    public static BigDecimal calcularValorEstadiaSuite(BigDecimal tarifaNoche, int noches) {
        BigDecimal subtotal = tarifaNoche.multiply(new BigDecimal(noches));
        BigDecimal recargo = subtotal.multiply(new BigDecimal("0.15"));
        BigDecimal conRecargo = subtotal.add(recargo);
        BigDecimal total = conRecargo;
        if (noches >= 5) {
            BigDecimal descuento = conRecargo.multiply(new BigDecimal("0.10"));
            total = conRecargo.subtract(descuento);
        }
        return redondear(total);
    }

    /**
     * Devuelve el desglose del valor de una estadía sencilla.
     *
     * @param tarifaNoche tarifa por noche
     * @param noches      número de noches
     * @return mapa con Subtotal y Total
     */
    public static Map<String, BigDecimal> desglosarSencilla(BigDecimal tarifaNoche, int noches) {
        BigDecimal total = calcularValorEstadiaSencilla(tarifaNoche, noches);
        Map<String, BigDecimal> desglose = new LinkedHashMap<>();
        desglose.put("Subtotal", total);
        desglose.put("Total", total);
        return desglose;
    }

    /**
     * Devuelve el desglose del valor de una estadía suite.
     *
     * @param tarifaNoche tarifa por noche
     * @param noches      número de noches
     * @return mapa con Subtotal, Recargo servicios, Descuento estadía larga y Total
     */
    public static Map<String, BigDecimal> desglosarSuite(BigDecimal tarifaNoche, int noches) {
        BigDecimal subtotal = tarifaNoche.multiply(new BigDecimal(noches));
        BigDecimal recargo = subtotal.multiply(new BigDecimal("0.15"));
        BigDecimal conRecargo = subtotal.add(recargo);
        BigDecimal descuento = BigDecimal.ZERO;
        if (noches >= 5) {
            descuento = conRecargo.multiply(new BigDecimal("0.10"));
        }
        BigDecimal total = conRecargo.subtract(descuento);
        Map<String, BigDecimal> desglose = new LinkedHashMap<>();
        desglose.put("Subtotal", redondear(subtotal));
        desglose.put("Recargo servicios", redondear(recargo));
        desglose.put("Descuento estadia larga", redondear(descuento));
        desglose.put("Total", redondear(total));
        return desglose;
    }

    /**
     * Determina si el estado de la habitación es mantenimiento.
     *
     * @param estado estado de la habitación
     * @return true si el estado es MANTENIMIENTO
     */
    public static boolean estaEnMantenimiento(EstadoHabitacion estado) {
        return EstadoHabitacion.MANTENIMIENTO.equals(estado);
    }
}
