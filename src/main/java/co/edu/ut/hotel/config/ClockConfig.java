package co.edu.ut.hotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Configuracion del reloj de la aplicacion.
 * Expone un bean {@link Clock} con la zona horaria de Colombia (America/Bogota)
 * para que el calculo de "hoy" sea determinista y pueda probarse con un reloj fijo.
 */
@Configuration
public class ClockConfig {

    /**
     * Bean de reloj con la zona horaria de Bogota.
     * Colombia no usa horario de verano, por lo que la zona es estable durante todo el año.
     *
     * @return reloj del sistema en la zona America/Bogota
     */
    @Bean
    public Clock hotelClock() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }

}
