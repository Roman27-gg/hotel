package co.edu.ut.hotel.service.mapper;

import co.edu.ut.hotel.service.dto.request.HabitacionRequest;
import co.edu.ut.hotel.service.dto.response.HabitacionResponse;
import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.entity.HabitacionSencilla;
import co.edu.ut.hotel.service.entity.HabitacionSuite;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias del mapper de habitaciones.
 */
class HabitacionMapperTest {

    private final HabitacionMapper mapper = new HabitacionMapper();

    @Test
    void toEntityCreaSencillaSegunTipo() {
        HabitacionRequest req = new HabitacionRequest();
        req.setNumero("101");
        req.setTipo("SENCILLA");
        req.setCapacidad(2);
        req.setTarifaNoche(new java.math.BigDecimal("80000"));
        req.setEstado("DISPONIBLE");

        Habitacion h = mapper.toEntity(req);
        assertInstanceOf(HabitacionSencilla.class, h);
        assertEquals("101", h.getNumero());
        assertEquals(2, h.getCapacidad());
    }

    @Test
    void toEntityCreaSuiteSegunTipo() {
        HabitacionRequest req = new HabitacionRequest();
        req.setNumero("201");
        req.setTipo("SUITE");
        req.setCapacidad(4);
        req.setTarifaNoche(new java.math.BigDecimal("200000"));
        req.setEstado("DISPONIBLE");

        Habitacion h = mapper.toEntity(req);
        assertInstanceOf(HabitacionSuite.class, h);
    }

    @Test
    void toResponseCopiaCampos() {
        HabitacionSencilla h = new HabitacionSencilla(1L, "101", 2, new java.math.BigDecimal("80000"), EstadoHabitacion.DISPONIBLE);
        HabitacionResponse r = mapper.toResponse(h);
        assertEquals(1L, r.id());
        assertEquals("101", r.numero());
        assertEquals(EstadoHabitacion.DISPONIBLE, r.estado());
    }
}
