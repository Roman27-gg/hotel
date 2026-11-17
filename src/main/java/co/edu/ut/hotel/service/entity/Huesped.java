package co.edu.ut.hotel.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Huesped del hotel.
 * El documento es único. La validación de formato se hace en HuespedServiceImpl vía HuespedUtil.
 */
@Entity
@Table(name = "huesped")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Huesped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "documento", nullable = false, unique = true, length = 20)
    private String documento;

    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 60)
    private String apellido;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    /**
     * Devuelve el nombre completo del huesped.
     *
     * @return nombre y apellido concatenados
     */
    public String getNombreCompleto() {
        return (nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido);
    }
}