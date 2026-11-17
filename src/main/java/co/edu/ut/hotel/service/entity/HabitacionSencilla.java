package co.edu.ut.hotel.service.entity;

import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Habitacion sencilla.
 */
@Entity
@DiscriminatorValue("SENCILLA")
@NoArgsConstructor
public class HabitacionSencilla extends Habitacion {

    public HabitacionSencilla(Long id, String numero, int capacidad, BigDecimal tarifaNoche, EstadoHabitacion estado) {
        super(id, numero, capacidad, tarifaNoche, estado, TipoHabitacion.SENCILLA);
    }
}