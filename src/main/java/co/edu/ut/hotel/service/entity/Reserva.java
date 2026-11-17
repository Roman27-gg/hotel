package co.edu.ut.hotel.service.entity;

import co.edu.ut.hotel.service.enums.EstadoReserva;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Reserva de una habitación para un huesped en un rango de fechas.
 * Solo contiene datos y getters/setters. Las validaciones y cálculos están en ReservaUtil.
 */
@Entity
@Table(name = "reserva")
@Getter
@Setter
@NoArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "huesped_id", nullable = false)
    private Huesped huesped;

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate fechaEntrada;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(name = "num_huespedes", nullable = false)
    private int numHuespedes;

    @Column(name = "estado", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EstadoReserva estado;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "penalizacion", nullable = false, precision = 12, scale = 2)
    private BigDecimal penalizacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "fecha_check_in")
    private LocalDateTime fechaCheckIn;

    @Column(name = "fecha_check_out")
    private LocalDateTime fechaCheckOut;

    public Reserva(Long id, Habitacion habitacion, Huesped huesped, LocalDate fechaEntrada, LocalDate fechaSalida,
                   int numHuespedes, EstadoReserva estado, BigDecimal valorTotal, BigDecimal penalizacion,
                   LocalDateTime fechaCreacion, LocalDateTime fechaCancelacion, LocalDateTime fechaCheckIn,
                   LocalDateTime fechaCheckOut) {
        this.id = id;
        this.habitacion = habitacion;
        this.huesped = huesped;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.numHuespedes = numHuespedes;
        this.estado = estado;
        this.valorTotal = valorTotal;
        this.penalizacion = penalizacion;
        this.fechaCreacion = fechaCreacion;
        this.fechaCancelacion = fechaCancelacion;
        this.fechaCheckIn = fechaCheckIn;
        this.fechaCheckOut = fechaCheckOut;
    }

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}