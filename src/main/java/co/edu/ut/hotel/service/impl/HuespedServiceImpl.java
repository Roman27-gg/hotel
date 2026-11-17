package co.edu.ut.hotel.service.impl;

import co.edu.ut.hotel.service.dto.request.HuespedRequest;
import co.edu.ut.hotel.service.dto.response.HuespedResponse;
import co.edu.ut.hotel.service.entity.Huesped;
import co.edu.ut.hotel.service.HuespedService;
import co.edu.ut.hotel.service.repository.HuespedDao;
import co.edu.ut.hotel.service.exception.HotelException;
import co.edu.ut.hotel.service.exception.RecursoNoEncontradoException;
import co.edu.ut.hotel.service.mapper.HuespedMapper;
import co.edu.ut.hotel.service.util.HuespedUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementacion del servicio de huespedes.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HuespedServiceImpl implements HuespedService {

    private final HuespedDao huespedDao;
    private final HuespedMapper huespedMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HuespedResponse> findAll() {
        return huespedMapper.toResponseList(huespedDao.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public HuespedResponse findById(Long id) {
        Huesped h = huespedDao.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Huesped no encontrado con id " + id));
        return huespedMapper.toResponse(h);
    }

    @Override
    @Transactional
    public HuespedResponse create(HuespedRequest request) {
        HuespedUtil.validarDocumento(request.getDocumento());
        if (huespedDao.findByDocumento(request.getDocumento()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un huesped con el documento " + request.getDocumento());
        }
        Huesped entity = huespedMapper.toEntity(request);
        Huesped saved = huespedDao.save(entity);
        log.info("Huesped creado: documento={} nombre={} {}", saved.getDocumento(), saved.getNombre(), saved.getApellido());
        return huespedMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public HuespedResponse update(Long id, HuespedRequest request) {
        Huesped existente = huespedDao.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Huesped no encontrado con id " + id));
        // Verificar si el documento ya existe en otro huesped
        huespedDao.findByDocumento(request.getDocumento())
                .filter(h -> !h.getId().equals(id))
                .ifPresent(h -> { throw new IllegalArgumentException("Ya existe un huesped con el documento " + request.getDocumento()); });
        // Actualizar campos
        existente.setDocumento(request.getDocumento());
        existente.setNombre(request.getNombre());
        existente.setApellido(request.getApellido());
        existente.setTelefono(request.getTelefono());
        existente.setEmail(request.getEmail());
        Huesped saved = huespedDao.save(existente);
        log.info("Huesped actualizado: id={} documento={}", saved.getId(), saved.getDocumento());
        return huespedMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!huespedDao.existsById(id)) {
            throw new RecursoNoEncontradoException("Huesped no encontrado con id " + id);
        }
        huespedDao.deleteById(id);
        log.info("Huesped eliminado: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public HuespedResponse findByDocument(String document) {
        Huesped h = huespedDao.findByDocumento(document)
                .orElseThrow(() -> new RecursoNoEncontradoException("Huesped no encontrado con documento " + document));
        return huespedMapper.toResponse(h);
    }
}
