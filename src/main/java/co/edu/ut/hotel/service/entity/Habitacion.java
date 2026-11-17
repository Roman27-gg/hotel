package co.edu.ut.hotel.service.entity;

import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Habitacion del hotel (clase abstracta mapeada con JPA).
 * Solo contiene datos y getters/setters. Las validaciones y cálculos están en HabitacionUtil.
 */
@Entity
@Table(name = "habitacion")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "numero", nullable = false, unique = true, length = 10)
    private String numero;

    @Column(name = "capacidad", nullable = false)
    private int capacidad;

    @Column(name = "tarifa_noche", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaNoche;

    @Column(name = "estado", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EstadoHabitacion estado;

    @Column(name = "tipo", nullable = false, length = 20, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TipoHabitacion tipo;
}