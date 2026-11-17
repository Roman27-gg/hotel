package co.edu.ut.hotel.service.repository;

import co.edu.ut.hotel.service.entity.Huesped;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositorio JPA de huespedes.
 * Expone operaciones CRUD y consulta por documento.
 */
public interface HuespedDao extends JpaRepository<Huesped, Long> {

    /**
     * Busca un huesped por su numero de documento.
     *
     * @param documento numero de documento
     * @return huesped encontrado, o vacio
     */
    @Query("SELECT h FROM Huesped h WHERE h.documento = :documento")
    Optional<Huesped> findByDocumento(@Param("documento") String documento);
}
