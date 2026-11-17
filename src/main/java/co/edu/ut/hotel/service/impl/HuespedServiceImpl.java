package co.edu.ut.hotel.service.impl;

import co.edu.ut.hotel.service.dto.request.HuespedRequest;
import co.edu.ut.hotel.service.dto.response.HuespedResponse;
import co.edu.ut.hotel.service.entity.Huesped;
import co.edu.ut.hotel.service.HuespedService;
import co.edu.ut.hotel.service.repository.HuespedDao;
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
    @Transactional(readOnly = true)
    public HuespedResponse findByDocument(String document) {
        Huesped h = huespedDao.findByDocumento(document)
                .orElseThrow(() -> new RecursoNoEncontradoException("Huesped no encontrado con documento " + document));
        return huespedMapper.toResponse(h);
    }
}
