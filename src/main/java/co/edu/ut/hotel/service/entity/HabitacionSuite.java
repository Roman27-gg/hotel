package co.edu.ut.hotel.service.entity;

import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Habitacion suite.
 */
@Entity
@DiscriminatorValue("SUITE")
@NoArgsConstructor
public class HabitacionSuite extends Habitacion {

    public static final BigDecimal RECARGO_SERVICIOS = new BigDecimal("0.15");
    public static final int NOCHES_PARA_DESCUENTO = 5;
    public static final BigDecimal DESCUENTO_ESTADIA_LARGA = new BigDecimal("0.10");

    public HabitacionSuite(Long id, String numero, int capacidad, BigDecimal tarifaNoche, EstadoHabitacion estado) {
        super(id, numero, capacidad, tarifaNoche, estado, TipoHabitacion.SUITE);
    }
}