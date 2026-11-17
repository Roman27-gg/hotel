package co.edu.ut.hotel.service.mapper;

import co.edu.ut.hotel.service.dto.request.ReservaRequest;
import co.edu.ut.hotel.service.dto.response.ReservaResponse;
import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.entity.HabitacionSencilla;
import co.edu.ut.hotel.service.entity.Huesped;
import co.edu.ut.hotel.service.entity.Reserva;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.EstadoReserva;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias del mapper de reservas.
 */
class ReservaMapperTest {

    private final ReservaMapper mapper = new ReservaMapper();

    @Test
    void toEntityConvierteCorrectamente() {
        Habitacion habitacion = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Huesped huesped = new Huesped();
        huesped.setId(1L);
        huesped.setDocumento("12345678");
        huesped.setNombre("Juan");
        huesped.setApellido("Perez");

        ReservaRequest request = new ReservaRequest();
        request.setHabitacionId(1L);
        request.setHuespedId(1L);
        request.setFechaEntrada(LocalDate.of(2026, 11, 20));
        request.setFechaSalida(LocalDate.of(2026, 11, 23));
        request.setNumHuespedes(2);
        request.setValorTotal(new BigDecimal("240000"));

        Reserva reserva = mapper.toEntity(request, habitacion, huesped);

        assertEquals(habitacion, reserva.getHabitacion());
        assertEquals(huesped, reserva.getHuesped());
        assertEquals(LocalDate.of(2026, 11, 20), reserva.getFechaEntrada());
        assertEquals(LocalDate.of(2026, 11, 23), reserva.getFechaSalida());
        assertEquals(2, reserva.getNumHuespedes());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(new BigDecimal("240000"), reserva.getValorTotal());
        assertEquals(BigDecimal.ZERO, reserva.getPenalizacion());
    }

    @Test
    void toResponseConvierteCorrectamente() {
        Habitacion habitacion = new HabitacionSencilla(1L, "101", 2, new BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        Huesped huesped = new Huesped();
        huesped.setId(1L);
        huesped.setDocumento("12345678");
        huesped.setNombre("Juan");
        huesped.setApellido("Perez");

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setHabitacion(habitacion);
        reserva.setHuesped(huesped);
        reserva.setFechaEntrada(LocalDate.of(2026, 11, 20));
        reserva.setFechaSalida(LocalDate.of(2026, 11, 23));
        reserva.setNumHuespedes(2);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setValorTotal(new BigDecimal("240000"));
        reserva.setPenalizacion(BigDecimal.ZERO);

        ReservaResponse response = mapper.toResponse(reserva);

        assertEquals(1L, response.id());
        assertEquals("101", response.habitacionNumero());
        assertEquals("Juan Perez", response.huespedNombreCompleto());
        assertEquals("12345678", response.huespedDocumento());
        assertEquals(LocalDate.of(2026, 11, 20), response.fechaEntrada());
        assertEquals(LocalDate.of(2026, 11, 23), response.fechaSalida());
        assertEquals(3, response.noches());
        assertEquals(2, response.numHuespedes());
        assertEquals(EstadoReserva.CONFIRMADA, response.estado());
        assertEquals(new BigDecimal("240000"), response.valorTotal());
        assertEquals(BigDecimal.ZERO, response.penalizacion());
        assertNotNull(response.desglose());
        assertEquals(2, response.desglose().size());
    }
}
