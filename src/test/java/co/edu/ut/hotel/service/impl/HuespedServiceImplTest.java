package co.edu.ut.hotel.service.impl;

import co.edu.ut.hotel.service.HuespedService;
import co.edu.ut.hotel.service.dto.request.HuespedRequest;
import co.edu.ut.hotel.service.dto.response.HuespedResponse;
import co.edu.ut.hotel.service.repository.HuespedDao;
import co.edu.ut.hotel.service.entity.Huesped;
import co.edu.ut.hotel.service.exception.RecursoNoEncontradoException;
import co.edu.ut.hotel.service.mapper.HuespedMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de HuespedServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class HuespedServiceImplTest {

    @Mock
    private HuespedDao huespedDao;

    @Mock
    private HuespedMapper huespedMapper;

    @InjectMocks
    private HuespedServiceImpl huespedService;

    @Test
    void findAllDevuelveLista() {
        when(huespedDao.findAll()).thenReturn(List.of());
        when(huespedMapper.toResponseList(any())).thenReturn(List.of());

        List<HuespedResponse> result = huespedService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdEncontrado() {
        Huesped h = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        when(huespedDao.findById(1L)).thenReturn(Optional.of(h));
        when(huespedMapper.toResponse(h)).thenReturn(new HuespedResponse(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com"));

        HuespedResponse result = huespedService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("12345678", result.documento());
    }

    @Test
    void findByIdNoEncontrado() {
        when(huespedDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> huespedService.findById(99L));
    }

    @Test
    void createExitoso() {
        HuespedRequest req = new HuespedRequest();
        req.setDocumento("12345678");
        req.setNombre("Juan");
        req.setApellido("Perez");
        req.setTelefono("3001112233");
        req.setEmail("juan@email.com");

        Huesped entity = new Huesped();
        Huesped saved = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        HuespedResponse resp = new HuespedResponse(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com");

        when(huespedDao.findByDocumento("12345678")).thenReturn(Optional.empty());
        when(huespedMapper.toEntity(req)).thenReturn(entity);
        when(huespedDao.save(entity)).thenReturn(saved);
        when(huespedMapper.toResponse(saved)).thenReturn(resp);

        HuespedResponse result = huespedService.create(req);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void createDocumentoDuplicado() {
        HuespedRequest req = new HuespedRequest();
        req.setDocumento("12345678");
        req.setNombre("Juan");
        req.setApellido("Perez");
        req.setTelefono("3001112233");

        Huesped existente = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        when(huespedDao.findByDocumento("12345678")).thenReturn(Optional.of(existente));

        assertThrows(IllegalArgumentException.class, () -> huespedService.create(req));
    }

    @Test
    void findByDocumentEncontrado() {
        Huesped h = new Huesped(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com", null);
        when(huespedDao.findByDocumento("12345678")).thenReturn(Optional.of(h));
        when(huespedMapper.toResponse(h)).thenReturn(new HuespedResponse(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com"));

        HuespedResponse result = huespedService.findByDocument("12345678");

        assertNotNull(result);
        assertEquals("12345678", result.documento());
    }

    @Test
    void findByDocumentNoEncontrado() {
        when(huespedDao.findByDocumento("99999999")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> huespedService.findByDocument("99999999"));
    }
}
