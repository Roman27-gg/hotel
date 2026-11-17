package co.edu.ut.hotel.controller.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Configuración de prueba para TableroControllerWeb.
 * Provee un bean Clock con hora fija para pruebas determinísticas.
 */
@Configuration
class TableroTestConfig {

    @Bean
    Clock clock() {
        return Clock.fixed(Instant.parse("2026-11-16T10:00:00Z"), ZoneId.of("America/Bogota"));
    }
}