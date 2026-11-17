package co.edu.ut.hotel.service.impl;

import co.edu.ut.hotel.service.CotizacionService;
import co.edu.ut.hotel.service.dto.response.CotizacionResponse;
import co.edu.ut.hotel.service.repository.HabitacionDao;
import co.edu.ut.hotel.service.entity.Habitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import co.edu.ut.hotel.service.exception.RecursoNoEncontradoException;
import co.edu.ut.hotel.service.util.HabitacionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Implementacion del servicio de cotizacion.
 * Calcula el valor de una estadia usando HabitacionUtil.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CotizacionServiceImpl implements CotizacionService {

    private final HabitacionDao habitacionDao;

    @Override
    @Transactional(readOnly = true)
    public CotizacionResponse calculate(Long roomId, int nights) {
        Habitacion h = habitacionDao.findById(roomId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Habitacion no encontrada con id " + roomId));
        Map<String, BigDecimal> desglose;
        if (h.getTipo() == TipoHabitacion.SENCILLA) {
            desglose = HabitacionUtil.desglosarSencilla(h.getTarifaNoche(), nights);
        } else {
            desglose = HabitacionUtil.desglosarSuite(h.getTarifaNoche(), nights);
        }
        BigDecimal total = desglose.get("Total");
        BigDecimal subtotal = desglose.getOrDefault("Subtotal", BigDecimal.ZERO);
        BigDecimal recargo = desglose.getOrDefault("Recargo servicios", BigDecimal.ZERO);
        BigDecimal descuento = desglose.getOrDefault("Descuento estadia larga", BigDecimal.ZERO);
        if (recargo == null) recargo = BigDecimal.ZERO;
        if (descuento == null) descuento = BigDecimal.ZERO;
        log.debug("Cotizacion habitacion={} noches={} total={}", h.getNumero(), nights, total);
        return new CotizacionResponse(
                nights,
                subtotal.setScale(2, RoundingMode.HALF_UP),
                recargo.setScale(2, RoundingMode.HALF_UP),
                descuento.setScale(2, RoundingMode.HALF_UP),
                total.setScale(2, RoundingMode.HALF_UP)
        );
    }
}
