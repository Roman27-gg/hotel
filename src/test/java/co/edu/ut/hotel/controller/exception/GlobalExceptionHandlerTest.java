package co.edu.ut.hotel.controller.exception;

import co.edu.ut.hotel.service.exception.CancelacionNoPermitidaException;
import co.edu.ut.hotel.service.exception.ErrorResponseDTO;
import co.edu.ut.hotel.service.exception.HabitacionConReservasException;
import co.edu.ut.hotel.service.exception.HabitacionNoDisponibleException;
import co.edu.ut.hotel.service.exception.RecursoNoEncontradoException;
import co.edu.ut.hotel.service.exception.ReservaCruzadaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del GlobalExceptionHandler.
 * Cubre el manejo global de excepciones y la generación de respuestas JSON.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private ApiExceptionHandler handler;

    @Mock
    private WebRequest request;

    @Test
    void manejarRecursoNoEncontradoDevuelve404() {
        when(request.getDescription(false)).thenReturn("/api/habitaciones/1");
        RecursoNoEncontradoException ex = new RecursoNoEncontradoException("Habitacion no encontrada");

        ResponseEntity<ErrorResponseDTO> response = handler.manejarHotelException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getCodigoDeError());
        assertEquals("Habitacion no encontrada", response.getBody().getDescripcion());
        assertNotNull(response.getBody().getHora());
    }

    @Test
    void manejarReservaCruzadaDevuelve409() {
        when(request.getDescription(false)).thenReturn("/api/reservas");
        ReservaCruzadaException ex = new ReservaCruzadaException("La habitacion ya tiene una reserva confirmada");

        ResponseEntity<ErrorResponseDTO> response = handler.manejarHotelException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getCodigoDeError());
        assertEquals("La habitacion ya tiene una reserva confirmada", response.getBody().getDescripcion());
    }

    @Test
    void manejarHabitacionNoDisponibleDevuelve400() {
        when(request.getDescription(false)).thenReturn("/api/habitaciones/1");
        HabitacionNoDisponibleException ex = new HabitacionNoDisponibleException("La habitacion esta en mantenimiento");

        ResponseEntity<ErrorResponseDTO> response = handler.manejarHotelException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCodigoDeError());
        assertEquals("La habitacion esta en mantenimiento", response.getBody().getDescripcion());
    }

    @Test
    void manejarCancelacionNoPermitidaDevuelve400() {
        when(request.getDescription(false)).thenReturn("/api/reservas/1/cancelar");
        CancelacionNoPermitidaException ex = new CancelacionNoPermitidaException("No se puede cancelar una reserva cuya estadia ya comenzo");

        ResponseEntity<ErrorResponseDTO> response = handler.manejarHotelException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCodigoDeError());
        assertEquals("No se puede cancelar una reserva cuya estadia ya comenzo", response.getBody().getDescripcion());
    }

    @Test
    void manejarHabitacionConReservasDevuelve400() {
        when(request.getDescription(false)).thenReturn("/api/habitaciones/1");
        HabitacionConReservasException ex = new HabitacionConReservasException("La habitacion tiene reservas asociadas");

        ResponseEntity<ErrorResponseDTO> response = handler.manejarHotelException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCodigoDeError());
        assertEquals("La habitacion tiene reservas asociadas", response.getBody().getDescripcion());
    }

    @Test
    void errorResponseDTOBuilder() {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigoDeError(404)
                .descripcion("Recurso no encontrado")
                .hora(java.time.LocalDateTime.now())
                .path("/api/habitaciones/1")
                .detalles(java.util.List.of("detalle 1", "detalle 2"))
                .build();

        assertEquals(404, error.getCodigoDeError());
        assertEquals("Recurso no encontrado", error.getDescripcion());
        assertNotNull(error.getHora());
        assertEquals("/api/habitaciones/1", error.getPath());
        assertEquals(2, error.getDetalles().size());
    }

    @Test
    void errorResponseDTONoArgsConstructor() {
        ErrorResponseDTO error = new ErrorResponseDTO();
        assertEquals(0, error.getCodigoDeError());
        assertNull(error.getDescripcion());
        assertNull(error.getHora());
        assertNull(error.getPath());
        assertNull(error.getDetalles());
    }
}
