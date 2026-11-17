package co.edu.ut.hotel.service.impl;

import co.edu.ut.hotel.service.dto.request.ReservaRequest;
import co.edu.ut.hotel.service.dto.response.ReservaResponse;
import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.entity.Huesped;
import co.edu.ut.hotel.service.entity.Reserva;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.EstadoReserva;
import co.edu.ut.hotel.service.exception.HabitacionNoDisponibleException;
import co.edu.ut.hotel.service.exception.RecursoNoEncontradoException;
import co.edu.ut.hotel.service.exception.ReservaCruzadaException;
import co.edu.ut.hotel.service.exception.SimulacionFalloException;
import co.edu.ut.hotel.service.ReservaService;
import co.edu.ut.hotel.service.CotizacionService;
import co.edu.ut.hotel.service.repository.HabitacionDao;
import co.edu.ut.hotel.service.repository.HuespedDao;
import co.edu.ut.hotel.service.repository.ReservaDao;
import co.edu.ut.hotel.service.mapper.ReservaMapper;
import co.edu.ut.hotel.service.util.HabitacionUtil;
import co.edu.ut.hotel.service.util.ReservaUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementacion del servicio de reservas.
 * Contiene la transaccion obligatoria de creacion de reserva (reserva + habitacion)
 * y las transacciones de cancelacion, check-in y check-out.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ReservaServiceImpl implements ReservaService {

    private final ReservaDao reservaDao;
    private final HabitacionDao habitacionDao;
    private final HuespedDao huespedDao;
    private final ReservaMapper reservaMapper;
    private final CotizacionService cotizacionService;
    private final Clock clock;

    @Value("${hotel.demo.simular-fallo-habilitado:false}")
    private boolean simularFalloHabilitado;

    @Override
    @Transactional
    public ReservaResponse create(ReservaRequest request) {
        // 1. Bloquear la fila de la habitacion para serializar reservas simultaneas (SELECT ... FOR UPDATE)
        Habitacion habitacion = habitacionDao.lock(request.getHabitacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Habitacion no encontrada con id " + request.getHabitacionId()));

        // 2. Validaciones de negocio
        if (HabitacionUtil.estaEnMantenimiento(habitacion.getEstado())) {
            throw new HabitacionNoDisponibleException("La habitacion " + habitacion.getNumero() + " esta en mantenimiento y no se puede reservar");
        }

        Huesped huesped = huespedDao.findById(request.getHuespedId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Huesped no encontrado con id " + request.getHuespedId()));

        LocalDate hoy = LocalDate.now(clock);
        if (request.getFechaEntrada().isBefore(hoy)) {
            throw new IllegalArgumentException("La fecha de entrada no puede ser anterior a hoy (" + hoy + ")");
        }
        ReservaUtil.validarFechas(request.getFechaEntrada(), request.getFechaSalida());
        ReservaUtil.validarCapacidad(request.getNumHuespedes(), habitacion.getCapacidad());

        // 3. Verificar cruce de fechas (red de seguridad en servicio y BD)
        if (reservaDao.existsOverlap(habitacion.getId(), request.getFechaEntrada(), request.getFechaSalida())) {
            throw new ReservaCruzadaException(
                    "La habitacion " + habitacion.getNumero() + " ya tiene una reserva confirmada en esas fechas");
        }

        // 4. Calcular valor total con polimorfismo
        int noches = HabitacionUtil.calcularNoches(request.getFechaEntrada(), request.getFechaSalida());
        BigDecimal valorTotal = cotizacionService.calculate(habitacion.getId(), noches).total();
        request.setValorTotal(valorTotal);

        // 5. Crear entidad y persistir (tabla 1: reserva)
        Reserva reserva = reservaMapper.toEntity(request, habitacion, huesped);
        Reserva saved = reservaDao.save(reserva);

        // 6. El estado de la habitacion no cambia al crear;
        // pasa a OCUPADA en el check-in y a DISPONIBLE en el check-out.

        // 7. Solo perfil demo: simular fallo despues de guardar para demostrar rollback
        if (simularFalloHabilitado && Boolean.TRUE.equals(request.getSimularFallo())) {
            log.warn("Simulando fallo despues de guardar reserva id={}", saved.getId());
            throw new SimulacionFalloException("Fallo simulado: se deshace la reserva y el estado de la habitacion");
        }

        log.info("Reserva creada: id={} habitacion={} valorTotal={}", saved.getId(), habitacion.getNumero(), valorTotal);
        return reservaMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> findAll() {
        return reservaMapper.toResponseList(reservaDao.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponse findById(Long id) {
        Reserva r = reservaDao.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada con id " + id));
        return reservaMapper.toResponse(r);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Reserva reserva = reservaDao.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada con id " + id));
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new IllegalArgumentException("Solo se puede cancelar una reserva CONFIRMADA");
        }

        LocalDate hoy = LocalDate.now(clock);
        BigDecimal penalizacion = ReservaUtil.calcularPenalizacion(hoy, reserva.getFechaEntrada(), reserva.getHabitacion().getTarifaNoche());
        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setPenalizacion(penalizacion);
        reserva.setFechaCancelacion(java.time.LocalDateTime.now(clock));
        reservaDao.save(reserva);

        recalcularEstadoHabitacion(reserva.getHabitacion().getId());

        log.info("Reserva cancelada: id={} penalizacion={}", id, penalizacion);
    }

    @Override
    @Transactional
    public void checkIn(Long reservaId) {
        Reserva reserva = reservaDao.findById(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada con id " + reservaId));
        ReservaUtil.checkIn(reserva);
        reservaDao.save(reserva);
        log.info("Check-in realizado: reservaId={}", reservaId);
    }

    @Override
    @Transactional
    public void checkOut(Long reservaId) {
        Reserva reserva = reservaDao.findById(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada con id " + reservaId));
        ReservaUtil.checkOut(reserva);
        reservaDao.save(reserva);
        recalcularEstadoHabitacion(reserva.getHabitacion().getId());
        log.info("Check-out realizado: reservaId={}", reservaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> findByStateAndDateRange(String state, LocalDate from, LocalDate to) {
        return reservaMapper.toResponseList(reservaDao.findAll());
    }

    /**
     * Recalcula el estado de la habitacion seguiendo si queda alguna reserva confirmada vigente o futura.
     *
     * @param roomId identificador de la habitacion
     */
    private void recalcularEstadoHabitacion(Long roomId) {
        var activas = reservaDao.findActiveByRoomId(roomId);
        String nuevoEstado = activas.isEmpty() ? EstadoHabitacion.DISPONIBLE.name() : EstadoHabitacion.RESERVADA.name();
        habitacionDao.updateState(roomId, nuevoEstado);
    }
}
