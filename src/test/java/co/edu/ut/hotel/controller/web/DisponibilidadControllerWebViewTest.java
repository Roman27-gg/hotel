package co.edu.ut.hotel.controller.web;

import co.edu.ut.hotel.service.DisponibilidadService;
import co.edu.ut.hotel.service.dto.response.HabitacionDisponibleResponse;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import co.edu.ut.hotel.test.PaginaAsserts;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de vista para DisponibilidadControllerWeb.
 */
@WebMvcTest(DisponibilidadControllerWeb.class)
@Import(ThymeleafAutoConfiguration.class)
class DisponibilidadControllerWebViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DisponibilidadService disponibilidadService;

    @Test
    void consultar_sinParametros_muestraFormularioVacio() throws Exception {
        MvcResult result = mockMvc.perform(get("/disponibilidad"))
                .andExpect(status().isOk())
                .andExpect(view().name("disponibilidad/lista"))
                .andExpect(model().attributeExists("tipos"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET /disponibilidad sin parámetros");
        // No debería haber tabla de disponibles
        var tabla = doc.select("table");
        if (!tabla.isEmpty()) {
            throw new AssertionError("Sin parámetros no debería haber tabla de resultados");
        }
    }

    @Test
    void consultar_fechaEntradaFaltante_muestraError() throws Exception {
        MvcResult result = mockMvc.perform(get("/disponibilidad")
                        .param("fechaSalida", "2026-12-25"))
                .andExpect(status().isOk())
                .andExpect(view().name("disponibilidad/lista"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET /disponibilidad sin fecha entrada");
        PaginaAsserts.errorGlobalVisible(doc, "debe mostrar error por fecha entrada faltante");
    }

    @Test
    void consultar_fechaSalidaAntesDeEntrada_muestraError() throws Exception {
        MvcResult result = mockMvc.perform(get("/disponibilidad")
                        .param("fechaEntrada", "2026-12-25")
                        .param("fechaSalida", "2026-12-20"))
                .andExpect(status().isOk())
                .andExpect(view().name("disponibilidad/lista"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET /disponibilidad salida < entrada");
        PaginaAsserts.errorGlobalVisible(doc, "debe mostrar error por salida anterior a entrada");
    }

    @Test
    void consultar_conResultados_muestraTabla() throws Exception {
        HabitacionDisponibleResponse disp = new HabitacionDisponibleResponse(
                1L, "101", TipoHabitacion.SENCILLA, 2, BigDecimal.valueOf(80000), EstadoHabitacion.DISPONIBLE, BigDecimal.valueOf(240000)
        );
        when(disponibilidadService.findAvailable(any(), any(), any(), any()))
                .thenReturn(List.of(disp));

        MvcResult result = mockMvc.perform(get("/disponibilidad")
                        .param("fechaEntrada", "2026-12-20")
                        .param("fechaSalida", "2026-12-25"))
                .andExpect(status().isOk())
                .andExpect(view().name("disponibilidad/lista"))
                .andExpect(model().attributeExists("disponibles", "mensaje", "tipos"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET /disponibilidad con resultados");
        var filas = doc.select("tbody tr");
        if (filas.size() != 1) {
            throw new AssertionError("Debería haber 1 fila en la tabla, hay " + filas.size());
        }
    }

    @Test
    void consultar_sinDisponibles_muestraMensaje() throws Exception {
        when(disponibilidadService.findAvailable(any(), any(), any(), any()))
                .thenReturn(List.of());

        MvcResult result = mockMvc.perform(get("/disponibilidad")
                        .param("fechaEntrada", "2026-12-20")
                        .param("fechaSalida", "2026-12-25"))
                .andExpect(status().isOk())
                .andExpect(view().name("disponibilidad/lista"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET /disponibilidad sin disponibles");
        // Verificar mensaje de "No hay habitaciones disponibles"
        var texto = doc.text();
        if (!texto.contains("No hay habitaciones disponibles")) {
            throw new AssertionError("Debería mostrar mensaje 'No hay habitaciones disponibles'");
        }
    }
}