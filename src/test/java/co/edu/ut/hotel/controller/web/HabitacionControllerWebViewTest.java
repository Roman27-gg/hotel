package co.edu.ut.hotel.controller.web;

import co.edu.ut.hotel.service.HabitacionService;
import co.edu.ut.hotel.service.dto.request.HabitacionRequest;
import co.edu.ut.hotel.service.dto.response.HabitacionResponse;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.TipoHabitacion;
import co.edu.ut.hotel.test.PaginaAsserts;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de vista para HabitacionControllerWeb.
 */
@WebMvcTest(HabitacionControllerWeb.class)
class HabitacionControllerWebViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HabitacionService habitacionService;

    @Test
    void nuevoForm_cargaCorrectamente() throws Exception {
        MvcResult result = mockMvc.perform(get("/habitaciones/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("habitacion/form"))
                .andExpect(model().attributeExists("habitacion", "tipos", "estados"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET /habitaciones/nuevo");
        PaginaAsserts.selectConOpciones(doc, "tipo", new String[]{"SENCILLA", "SUITE"}, "tipos en form habitacion");
        PaginaAsserts.selectConOpciones(doc, "estado", new String[]{"DISPONIBLE", "RESERVADA", "OCUPADA", "MANTENIMIENTO"}, "estados en form habitacion");
    }

    @Test
    void guardar_numeroDuplicado_muestraError() throws Exception {
        when(habitacionService.create(any())).thenThrow(
                new IllegalArgumentException("Ya existe una habitación con el número 101")
        );

        MvcResult result = mockMvc.perform(post("/habitaciones/guardar")
                        .param("numero", "101")
                        .param("tipo", "SENCILLA")
                        .param("capacidad", "2")
                        .param("tarifaNoche", "80000")
                        .param("estado", "DISPONIBLE"))
                .andExpect(status().isOk())
                .andExpect(view().name("habitacion/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "POST /habitaciones/guardar número duplicado");
        PaginaAsserts.errorGlobalVisible(doc, "debe mostrar error amigable de duplicado");
        // Verificar que se conservan los valores
        var inputNumero = doc.select("input[name=numero]").first();
        assertNotNull(inputNumero);
        assertEquals("101", inputNumero.attr("value"));
    }

    @Test
    void guardar_capacidadInvalida_muestraErrorCampo() throws Exception {
        MvcResult result = mockMvc.perform(post("/habitaciones/guardar")
                        .param("numero", "101")
                        .param("tipo", "SENCILLA")
                        .param("capacidad", "10") // > 6
                        .param("tarifaNoche", "80000")
                        .param("estado", "DISPONIBLE"))
                .andExpect(status().isOk())
                .andExpect(view().name("habitacion/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "POST /habitaciones/guardar capacidad > 6");
        PaginaAsserts.campoInvalidoConMensaje(doc, "capacidad", "capacidad > 6 debe mostrar error");
    }

    @Test
    void guardar_tarifaInvalida_muestraErrorCampo() throws Exception {
        MvcResult result = mockMvc.perform(post("/habitaciones/guardar")
                        .param("numero", "101")
                        .param("tipo", "SENCILLA")
                        .param("capacidad", "2")
                        .param("tarifaNoche", "-100") // negativa
                        .param("estado", "DISPONIBLE"))
                .andExpect(status().isOk())
                .andExpect(view().name("habitacion/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "POST /habitaciones/guardar tarifa negativa");
        PaginaAsserts.campoInvalidoConMensaje(doc, "tarifaNoche", "tarifa negativa debe mostrar error");
    }

    @Test
    void editarForm_cargaDatosExistentes() throws Exception {
        HabitacionResponse existente = new HabitacionResponse(
                1L, "101", TipoHabitacion.SENCILLA, 2, BigDecimal.valueOf(80000), EstadoHabitacion.DISPONIBLE
        );
        when(habitacionService.findById(1L)).thenReturn(existente);

        MvcResult result = mockMvc.perform(get("/habitaciones/editar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("habitacion/form"))
                .andExpect(model().attributeExists("habitacion", "id", "tipos", "estados"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET /habitaciones/editar/1");
        // Verificar que se cargan los valores existentes
        // El input numero debería tener value="101"
        var inputNumero = doc.select("input[name=numero]").first();
        if (inputNumero == null || !"101".equals(inputNumero.attr("value"))) {
            throw new AssertionError("Editar: número no se conserva (value=" + (inputNumero != null ? inputNumero.attr("value") : "null") + ")");
        }
    }

    @Test
    void actualizar_exito_redirigeConFlash() throws Exception {
        when(habitacionService.update(anyLong(), any())).thenReturn(
                new HabitacionResponse(1L, "101", TipoHabitacion.SENCILLA, 2, BigDecimal.valueOf(90000), EstadoHabitacion.DISPONIBLE)
        );

        MvcResult result = mockMvc.perform(post("/habitaciones/actualizar/1")
                        .param("numero", "101")
                        .param("tipo", "SENCILLA")
                        .param("capacidad", "2")
                        .param("tarifaNoche", "90000")
                        .param("estado", "DISPONIBLE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/habitaciones"))
                .andExpect(flash().attributeExists("mensaje"))
                .andReturn();
    }

    @Test
    void listar_conFiltros_funciona() throws Exception {
        when(habitacionService.findAll()).thenReturn(List.of());
        when(habitacionService.findByTypeAndState(any(), any())).thenReturn(List.of());

        MvcResult result = mockMvc.perform(get("/habitaciones")
                        .param("tipo", "SENCILLA")
                        .param("estado", "DISPONIBLE"))
                .andExpect(status().isOk())
                .andExpect(view().name("habitacion/lista"))
                .andExpect(model().attributeExists("habitaciones", "tipoFiltro", "estadoFiltro"))
                .andReturn();

        PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET /habitaciones con filtros");
    }
}