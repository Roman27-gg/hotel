package co.edu.ut.hotel.service.repository;

import co.edu.ut.hotel.service.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio JPA de reservas.
 * Expone operaciones CRUD y consultas propias con JPQL parametrizado:
 * cruce de fechas y reservas activas de una habitacion.
 */
public interface ReservaDao extends JpaRepository<Reserva, Long> {

    /**
     * Indica si existe una reserva confirmada (no cancelada ni finalizada) que se cruce en fechas
     * con el rango dado para la misma habitacion.
     * El rango es [checkIn, checkOut): la salida de una puede ser la entrada de otra.
     *
     * @param roomId identificador de la habitacion
     * @param checkIn fecha de entrada
     * @param checkOut fecha de salida
     * @return true si hay cruce de reservas
     */
    @Query("""
        SELECT case when count(r) > 0 then true else false end FROM Reserva r
        WHERE r.habitacion.id = :roomId
          AND r.estado = 'CONFIRMADA'
          AND r.fechaEntrada < :checkOut
          AND r.fechaSalida > :checkIn
        """)
    boolean existsOverlap(@Param("roomId") Long roomId,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut);

    /**
     * Lista las reservas confirmadas vigentes o futuras de una habitacion.
     * Se usa para recalcular el estado de la habitacion al cancelar.
     *
     * @param roomId identificador de la habitacion
     * @return lista de reservas activas
     */
    @Query("""
        SELECT r FROM Reserva r
        WHERE r.habitacion.id = :roomId
          AND r.estado = 'CONFIRMADA'
          AND r.fechaSalida > CURRENT_DATE
        ORDER BY r.fechaEntrada
        """)
    List<Reserva> findActiveByRoomId(@Param("roomId") Long roomId);
}
