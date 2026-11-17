package co.edu.ut.hotel.service.repository;

import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

/**
 * Repositorio JPA de habitaciones.
 * Expone operaciones CRUD y consultas propias con JPQL parametrizado.
 */
public interface HabitacionDao extends JpaRepository<Habitacion, Long> {

    /**
     * Busca una habitacion por su numero.
     *
     * @param numero numero de habitacion
     * @return habitacion encontrada, o vacia
     */
    @Query("SELECT h FROM Habitacion h WHERE h.numero = :numero")
    Optional<Habitacion> findByNumero(@Param("numero") String numero);

    /**
     * Busca habitaciones por tipo y estado (ambos opcionales).
     *
     * @param type  tipo de habitacion (opcional)
     * @param state estado de la habitacion (opcional)
     * @return lista de habitaciones
     */
    @Query("SELECT h FROM Habitacion h WHERE (:type IS NULL OR h.tipo = :type) AND (:state IS NULL OR h.estado = :state)")
    List<Habitacion> findByTypeAndState(@Param("type") TipoHabitacion type, @Param("state") EstadoHabitacion state);

    /**
     * Cuenta cuantas reservas tiene una habitacion (cualquier estado).
     * Se usa para decidir si se puede eliminar la habitacion.
     *
     * @param roomId identificador de la habitacion
     * @return cantidad de reservas
     */
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.habitacion.id = :roomId")
    int countReservasByRoom(@Param("roomId") Long roomId);

    /**
     * Bloquea la fila de la habitacion para serializar reservas simultaneas.
     * Usa bloqueo pesimista (SELECT ... FOR UPDATE).
     *
     * @param id identificador de la habitacion
     * @return habitacion bloqueada
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM Habitacion h WHERE h.id = :id")
    Optional<Habitacion> lock(@Param("id") Long id);

    /**
     * Actualiza el estado de una habitacion.
     *
     * @param roomId identificador de la habitacion
     * @param state  nuevo estado
     */
    @Transactional
    @Query("UPDATE Habitacion h SET h.estado = :state WHERE h.id = :roomId")
    void updateState(@Param("roomId") Long roomId, @Param("state") String state);
}
