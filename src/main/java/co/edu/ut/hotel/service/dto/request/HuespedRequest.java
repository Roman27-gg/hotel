package co.edu.ut.hotel.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para crear o actualizar un huesped.
 */
@Getter
@Setter
@NoArgsConstructor
public class HuespedRequest {

    @NotBlank(message = "El documento es obligatorio")
    @Size(min = 5, max = 20, message = "El documento debe tener entre 5 y 20 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "El documento debe contener solo letras y digitos")
    private String documento;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El telefono es obligatorio")
    private String telefono;

    @Email(message = "El correo electronico no es valido")
    private String email;
}
