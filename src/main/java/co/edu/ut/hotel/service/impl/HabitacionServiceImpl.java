package co.edu.ut.hotel.service.impl;

import co.edu.ut.hotel.service.dto.request.HabitacionRequest;
import co.edu.ut.hotel.service.dto.response.HabitacionResponse;
import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.exception.HabitacionConReservasException;
import co.edu.ut.hotel.service.exception.RecursoNoEncontradoException;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import co.edu.ut.hotel.service.HabitacionService;
import co.edu.ut.hotel.service.repository.HabitacionDao;
import co.edu.ut.hotel.service.mapper.HabitacionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementacion del servicio de habitaciones.
 * Contiene las reglas de negocio y las transacciones de escritura.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HabitacionServiceImpl implements HabitacionService {

    private final HabitacionDao habitacionDao;
    private final HabitacionMapper habitacionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionResponse> findAll() {
        return habitacionMapper.toResponseList(habitacionDao.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public HabitacionResponse findById(Long id) {
        Habitacion h = habitacionDao.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Habitacion no encontrada con id " + id));
        return habitacionMapper.toResponse(h);
    }

    @Override
    @Transactional
    public HabitacionResponse create(HabitacionRequest request) {
        if (habitacionDao.findByNumero(request.getNumero()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una habitacion con el numero " + request.getNumero());
        }
        Habitacion entity = habitacionMapper.toEntity(request);
        Habitacion saved = habitacionDao.save(entity);
        log.info("Habitacion creada: numero={} tipo={}", saved.getNumero(), saved.getTipo());
        return habitacionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public HabitacionResponse update(Long id, HabitacionRequest request) {
        if (!habitacionDao.findById(id).isPresent()) {
            throw new RecursoNoEncontradoException("Habitacion no encontrada con id " + id);
        }
        // Verificar numero duplicado excluyendo la habitacion actual
        habitacionDao.findByNumero(request.getNumero())
                .filter(h -> !h.getId().equals(id))
                .ifPresent(h -> { throw new IllegalArgumentException("Ya existe una habitacion con el numero " + request.getNumero()); });
        Habitacion entity = habitacionMapper.toEntity(request);
        entity.setId(id);
        Habitacion saved = habitacionDao.save(entity);
        return habitacionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!habitacionDao.findById(id).isPresent()) {
            throw new RecursoNoEncontradoException("Habitacion no encontrada con id " + id);
        }
        int reservas = habitacionDao.countReservasByRoom(id);
        if (reservas > 0) {
            throw new HabitacionConReservasException(
                    "No se puede eliminar la habitacion porque tiene " + reservas + " reserva(s) asociada(s). Considere ponerla en MANTENIMIENTO.");
        }
        habitacionDao.deleteById(id);
        log.info("Habitacion eliminada: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionResponse> findByTypeAndState(String type, String state) {
        TipoHabitacion tipo = (type == null || type.isEmpty()) ? null : TipoHabitacion.valueOf(type);
        EstadoHabitacion estado = (state == null || state.isEmpty()) ? null : EstadoHabitacion.valueOf(state);
        return habitacionMapper.toResponseList(habitacionDao.findByTypeAndState(tipo, estado));
    }
}
