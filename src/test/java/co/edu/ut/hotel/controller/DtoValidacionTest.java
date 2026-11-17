package co.edu.ut.hotel.controller;

import co.edu.ut.hotel.service.dto.request.HabitacionRequest;
import co.edu.ut.hotel.service.dto.request.ReservaRequest;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de validacion Bean Validation sobre los DTOs de entrada.
 */
class DtoValidacionTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void habitacionRequestValidoNoTieneViolaciones() {
        HabitacionRequest req = new HabitacionRequest();
        req.setNumero("101");
        req.setTipo("SENCILLA");
        req.setCapacidad(2);
        req.setTarifaNoche(new BigDecimal("80000"));
        req.setEstado("DISPONIBLE");
        Set<ConstraintViolation<HabitacionRequest>> viol = validator.validate(req);
        assertTrue(viol.isEmpty());
    }

    @Test
    void habitacionRequestNumeroVacioTieneViolacion() {
        HabitacionRequest req = new HabitacionRequest();
        req.setNumero("");
        Set<ConstraintViolation<HabitacionRequest>> viol = validator.validate(req);
        assertFalse(viol.isEmpty());
        assertTrue(viol.stream().anyMatch(v -> v.getPropertyPath().toString().equals("numero")));
    }

    @Test
    void reservaRequestSalidaAntesQueEntradaTieneViolacion() {
        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(1L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2026, 11, 20));
        req.setFechaSalida(LocalDate.of(2026, 11, 19));
        req.setNumHuespedes(1);
        Set<ConstraintViolation<ReservaRequest>> viol = validator.validate(req);
        assertFalse(viol.isEmpty());
    }

    @Test
    void reservaRequestEntradaAnteriorAHoyTieneViolacion() {
        ReservaRequest req = new ReservaRequest();
        req.setHabitacionId(1L);
        req.setHuespedId(1L);
        req.setFechaEntrada(LocalDate.of(2020, 1, 1));
        req.setFechaSalida(LocalDate.of(2020, 1, 2));
        req.setNumHuespedes(1);
        Set<ConstraintViolation<ReservaRequest>> viol = validator.validate(req);
        assertFalse(viol.isEmpty());
    }
}
