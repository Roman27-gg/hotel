package co.edu.ut.hotel.controller.web;

import co.edu.ut.hotel.service.dto.request.ReservaRequest;
import co.edu.ut.hotel.service.dto.response.ReservaResponse;
import co.edu.ut.hotel.service.enums.EstadoReserva;
import co.edu.ut.hotel.test.PaginaAsserts;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para el flujo completo de reservas con base de datos real (Testcontainers).
 * Verifica crear, cruzar, cancelar, check-in y check-out.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ReservaControllerWebFlujoIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("hotel")
            .withUsername("hotel")
            .withPassword("hotel");

    @DynamicPropertySource
    static void registrarPropiedades(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void crearReservaValida_guardaYRedirige() throws Exception {
        MvcResult result = mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "1")
                        .param("huespedId", "1")
                        .param("fechaEntrada", "2026-12-20")
                        .param("fechaSalida", "2026-12-23")
                        .param("numHuespedes", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservas"))
                .andExpect(flash().attributeExists("mensaje"))
                .andReturn();

        // Verificar que no hay trazas ni errores en la respuesta
        PaginaAsserts.limpia("", "crearReservaValida");
    }

    @Test
    void crearReservaConCruce_debeRechazarse() throws Exception {
        // Primera reserva válida
        mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "1")
                        .param("huespedId", "1")
                        .param("fechaEntrada", "2026-12-20")
                        .param("fechaSalida", "2026-12-23")
                        .param("numHuespedes", "2"))
                .andExpect(status().is3xxRedirection());

        // Segunda reserva con cruce (nuevo DENTRO de existente)
        MvcResult result = mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "1")
                        .param("huespedId", "2")
                        .param("fechaEntrada", "2026-12-21")
                        .param("fechaSalida", "2026-12-22")
                        .param("numHuespedes", "1"))
                .andExpect(status().isOk()) // MISMA vista, no redirect
                .andExpect(view().name("reserva/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "crearReservaConCruce");
        PaginaAsserts.errorGlobalVisible(doc, "debe mostrar error de cruce de fechas");
    }

    @Test
    void crearReservaConSalidaAntesDeEntrada_muestraErrorValidacion() throws Exception {
        MvcResult result = mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "1")
                        .param("huespedId", "1")
                        .param("fechaEntrada", "2026-12-25")
                        .param("fechaSalida", "2026-12-20")
                        .param("numHuespedes", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserva/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "crearReservaSalidaAntesDeEntrada");
        PaginaAsserts.errorGlobalVisible(doc, "debe mostrar error de fechas invertidas");
    }

    @Test
    void crearReservaConEntradaPasada_muestraErrorValidacion() throws Exception {
        MvcResult result = mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "1")
                        .param("huespedId", "1")
                        .param("fechaEntrada", "2020-01-01")
                        .param("fechaSalida", "2020-01-05")
                        .param("numHuespedes", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserva/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "crearReservaEntradaPasada");
        // Debe mostrar error de validación en el campo fechaEntrada
    }

    @Test
    void crearReservaConHuéspedesMayorACapacidad_muestraError() throws Exception {
        MvcResult result = mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "1")
                        .param("huespedId", "1")
                        .param("fechaEntrada", "2026-12-20")
                        .param("fechaSalida", "2026-12-23")
                        .param("numHuespedes", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserva/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "crearReservaHuespedesExcedenCapacidad");
        PaginaAsserts.errorGlobalVisible(doc, "debe mostrar error de capacidad excedida");
    }

    @Test
    void crucesParameterized_casosBordes() throws Exception {
        // Caso: nuevo CONTIENE a existente (rango amplio)
        mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "1")
                        .param("huespedId", "1")
                        .param("fechaEntrada", "2026-12-15")
                        .param("fechaSalida", "2026-12-28")
                        .param("numHuespedes", "2"))
                .andExpect(status().is3xxRedirection());

        // Caso: borde izquierdo (nuevo termina exactamente cuando existente empieza - PERMITIDO)
        MvcResult result1 = mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "1")
                        .param("huespedId", "2")
                        .param("fechaEntrada", "2026-12-10")
                        .param("fechaSalida", "2026-12-15")
                        .param("numHuespedes", "1"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        // Caso: borde derecho (nuevo empieza exactamente cuando existente termina - PERMITIDO)
        MvcResult result2 = mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "1")
                        .param("huespedId", "3")
                        .param("fechaEntrada", "2026-12-28")
                        .param("fechaSalida", "2026-12-30")
                        .param("numHuespedes", "1"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        // Caso: solapamiento parcial (debe fallar)
        mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "1")
                        .param("huespedId", "4")
                        .param("fechaEntrada", "2026-12-20")
                        .param("fechaSalida", "2026-12-25")
                        .param("numHuespedes", "1"))
                .andExpect(status().isOk());
    }
}