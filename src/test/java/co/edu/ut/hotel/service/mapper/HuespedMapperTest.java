package co.edu.ut.hotel.service.mapper;

import co.edu.ut.hotel.service.dto.request.HuespedRequest;
import co.edu.ut.hotel.service.dto.response.HuespedResponse;
import co.edu.ut.hotel.service.entity.Huesped;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias del mapper de huespedes.
 */
class HuespedMapperTest {

    private final HuespedMapper mapper = new HuespedMapper();

    @Test
    void toEntityConvierteCorrectamente() {
        HuespedRequest request = new HuespedRequest();
        request.setDocumento("12345678");
        request.setNombre("Juan");
        request.setApellido("Perez");
        request.setTelefono("3001112233");
        request.setEmail("juan.perez@email.com");

        Huesped entity = mapper.toEntity(request);

        assertEquals("12345678", entity.getDocumento());
        assertEquals("Juan", entity.getNombre());
        assertEquals("Perez", entity.getApellido());
        assertEquals("3001112233", entity.getTelefono());
        assertEquals("juan.perez@email.com", entity.getEmail());
        assertNotNull(entity.getFechaRegistro());
    }

    @Test
    void toResponseConvierteCorrectamente() {
        Huesped entity = new Huesped();
        entity.setId(1L);
        entity.setDocumento("12345678");
        entity.setNombre("Juan");
        entity.setApellido("Perez");
        entity.setTelefono("3001112233");
        entity.setEmail("juan.perez@email.com");
        entity.setFechaRegistro(LocalDateTime.now());

        HuespedResponse response = mapper.toResponse(entity);

        assertEquals(1L, response.id());
        assertEquals("12345678", response.documento());
        assertEquals("Juan", response.nombre());
        assertEquals("Perez", response.apellido());
        assertEquals("3001112233", response.telefono());
        assertEquals("juan.perez@email.com", response.email());
    }

    @Test
    void getNombreCompleto() {
        Huesped entity = new Huesped();
        entity.setNombre("Juan");
        entity.setApellido("Perez");
        assertEquals("Juan Perez", entity.getNombreCompleto());
    }

    @Test
    void getNombreCompletoSinApellido() {
        Huesped entity = new Huesped();
        entity.setNombre("Juan");
        assertEquals("Juan ", entity.getNombreCompleto());
    }

    @Test
    void getNombreCompletoSinNombre() {
        Huesped entity = new Huesped();
        entity.setApellido("Perez");
        assertEquals(" Perez", entity.getNombreCompleto());
    }
}
