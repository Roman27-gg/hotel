package co.edu.ut.hotel.controller.exception;

import co.edu.ut.hotel.service.exception.CancelacionNoPermitidaException;
import co.edu.ut.hotel.service.exception.ErrorResponseDTO;
import co.edu.ut.hotel.service.exception.HabitacionConReservasException;
import co.edu.ut.hotel.service.exception.HabitacionNoDisponibleException;
import co.edu.ut.hotel.service.exception.HotelException;
import co.edu.ut.hotel.service.exception.RecursoNoEncontradoException;
import co.edu.ut.hotel.service.exception.ReservaCruzadaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador de excepciones global.
 * - Para controladores web (@Controller en paquete controller.web): renderiza vistas HTML con mensajes amigables.
 * - Para APIs REST (@RestController): devuelve JSON con ErrorResponseDTO.
 * Nunca expone trazas ni detalles técnicos de SQL al cliente.
 */

// Advice para controladores WEB (Thymeleaf) - renderiza vistas HTML
@Slf4j
@ControllerAdvice(basePackages = "co.edu.ut.hotel.controller.web")
class WebExceptionHandler {

    @ExceptionHandler(HotelException.class)
    public ModelAndView manejarHotelException(HotelException ex, WebRequest request) {
        HttpStatus status = determinarEstado(ex);
        log.warn("Error de negocio (web): {} - {}", status.value(), ex.getMessage());
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", status.value());
        mav.addObject("error", ex.getMessage());
        mav.addObject("path", request.getDescription(false));
        mav.setStatus(status);
        return mav;
    }

    @ExceptionHandler(DataAccessException.class)
    public ModelAndView manejarDataAccess(DataAccessException ex, WebRequest request) {
        String mensaje = "No se pudo completar la operación con la base de datos. Intente de nuevo.";
        if (ex.getMessage() != null && ex.getMessage().contains("ex_reserva_sin_cruce")) {
            mensaje = "La habitación ya tiene una reserva confirmada en esas fechas";
        }
        log.error("Error de base de datos (web): ", ex);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        mav.addObject("error", mensaje);
        mav.addObject("path", request.getDescription(false));
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView manejarExcepcionInesperada(Exception ex, WebRequest request) {
        log.error("Error inesperado (web): ", ex);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        mav.addObject("error", "Ocurrió un error inesperado. El detalle fue registrado en el log.");
        mav.addObject("path", request.getDescription(false));
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return mav;
    }

    private HttpStatus determinarEstado(HotelException ex) {
        if (ex instanceof RecursoNoEncontradoException) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof ReservaCruzadaException) {
            return HttpStatus.CONFLICT;
        }
        if (ex instanceof HabitacionNoDisponibleException
                || ex instanceof CancelacionNoPermitidaException
                || ex instanceof HabitacionConReservasException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.BAD_REQUEST;
    }
}

// Advice para APIs REST (@RestController) - devuelve JSON
@Slf4j
@RestControllerAdvice(annotations = org.springframework.web.bind.annotation.RestController.class)
class ApiExceptionHandler {

    @ExceptionHandler(HotelException.class)
    public ResponseEntity<ErrorResponseDTO> manejarHotelException(HotelException ex, WebRequest request) {
        HttpStatus status = determinarEstado(ex);
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigoDeError(status.value())
                .descripcion(ex.getMessage())
                .hora(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        log.warn("Error de negocio (API): {} - {}", status.value(), ex.getMessage());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarValidacion(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigoDeError(HttpStatus.BAD_REQUEST.value())
                .descripcion("Errores de validación en los datos de entrada")
                .hora(LocalDateTime.now())
                .path(request.getDescription(false))
                .detalles(detalles)
                .build();
        log.warn("Error de validación (API): {}", detalles);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> manejarConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<String> detalles = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .toList();
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigoDeError(HttpStatus.BAD_REQUEST.value())
                .descripcion("Errores de validación en los datos de entrada")
                .hora(LocalDateTime.now())
                .path(request.getDescription(false))
                .detalles(detalles)
                .build();
        log.warn("Error de restricción (API): {}", detalles);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> manejarDataAccess(DataAccessException ex, WebRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigoDeError(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .descripcion("No se pudo completar la operación con la base de datos. Intente de nuevo.")
                .hora(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        log.error("Error de base de datos (API): ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarExcepcionInesperada(Exception ex, WebRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigoDeError(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .descripcion("Ocurrió un error inesperado. El detalle fue registrado en el log.")
                .hora(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        log.error("Error inesperado (API): ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private HttpStatus determinarEstado(HotelException ex) {
        if (ex instanceof RecursoNoEncontradoException) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof ReservaCruzadaException) {
            return HttpStatus.CONFLICT;
        }
        if (ex instanceof HabitacionNoDisponibleException
                || ex instanceof CancelacionNoPermitidaException
                || ex instanceof HabitacionConReservasException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.BAD_REQUEST;
    }
}