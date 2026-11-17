package co.edu.ut.hotel.service.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO especializado para devolver errores al cliente en formato JSON.
 * Contiene los campos estandarizados de error junto con metadatos adicionales.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {

    private int codigoDeError;
    private String descripcion;
    private LocalDateTime hora;
    private String path;
    private List<String> detalles;

}
