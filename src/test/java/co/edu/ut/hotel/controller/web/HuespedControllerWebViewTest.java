package co.edu.ut.hotel.controller.web;

import co.edu.ut.hotel.service.HuespedService;
import co.edu.ut.hotel.service.dto.request.HuespedRequest;
import co.edu.ut.hotel.service.dto.response.HuespedResponse;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de vista para HuespedControllerWeb.
 */
@WebMvcTest(HuespedControllerWeb.class)
@Import(ThymeleafAutoConfiguration.class)
class HuespedControllerWebViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HuespedService huespedService;

    @Test
    void nuevoForm_cargaCorrectamente() throws Exception {
        MvcResult result = mockMvc.perform(get("/huespedes/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("huesped/form"))
                .andExpect(model().attributeExists("huesped"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET /huespedes/nuevo");
    }

    @Test
    void guardar_documentoDuplicado_muestraError() throws Exception {
        when(huespedService.create(any())).thenThrow(
                new IllegalArgumentException("Ya existe un huésped con el documento 12345678")
        );

        MvcResult result = mockMvc.perform(post("/huespedes/guardar")
                        .param("documento", "12345678")
                        .param("nombre", "Juan")
                        .param("apellido", "Perez")
                        .param("telefono", "3001112233")
                        .param("email", "juan@email.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("huesped/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "POST /huespedes/guardar documento duplicado");
        PaginaAsserts.errorGlobalVisible(doc, "debe mostrar error amigable de duplicado");
    }

    @Test
    void guardar_correoInvalido_muestraErrorCampo() throws Exception {
        MvcResult result = mockMvc.perform(post("/huespedes/guardar")
                        .param("documento", "99999999")
                        .param("nombre", "Juan")
                        .param("apellido", "Perez")
                        .param("telefono", "3001112233")
                        .param("email", "correo-invalido"))
                .andExpect(status().isOk())
                .andExpect(view().name("huesped/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "POST /huespedes/guardar correo inválido");
        PaginaAsserts.campoInvalidoConMensaje(doc, "email", "correo inválido debe mostrar error");
    }

    @Test
    void guardar_documentoCorto_muestraErrorCampo() throws Exception {
        MvcResult result = mockMvc.perform(post("/huespedes/guardar")
                        .param("documento", "123") // < 5 chars
                        .param("nombre", "Juan")
                        .param("apellido", "Perez")
                        .param("telefono", "3001112233"))
                .andExpect(status().isOk())
                .andExpect(view().name("huesped/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "POST /huespedes/guardar documento < 5 chars");
        PaginaAsserts.campoInvalidoConMensaje(doc, "documento", "documento < 5 chars debe mostrar error");
    }

    @Test
    void listar_conBusqueda_funciona() throws Exception {
        when(huespedService.findAll()).thenReturn(List.of());

        MvcResult result = mockMvc.perform(get("/huespedes")
                        .param("buscar", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("huesped/lista"))
                .andExpect(model().attributeExists("huespedes", "buscar"))
                .andReturn();

        PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET /huespedes con búsqueda");
    }

    @Test
    void editarForm_cargaDatosExistentes() throws Exception {
        HuespedResponse existente = new HuespedResponse(
                1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com"
        );
        when(huespedService.findById(1L)).thenReturn(existente);

        MvcResult result = mockMvc.perform(get("/huespedes/editar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("huesped/form"))
                .andExpect(model().attributeExists("huesped", "id"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET /huespedes/editar/1");
        var inputDocumento = doc.select("input[name=documento]").first();
        if (inputDocumento == null || !"12345678".equals(inputDocumento.attr("value"))) {
            throw new AssertionError("Editar: documento no se conserva");
        }
    }

    @Test
    void actualizar_exito_redirigeConFlash() throws Exception {
        when(huespedService.update(anyLong(), any())).thenReturn(
                new HuespedResponse(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com")
        );

        MvcResult result = mockMvc.perform(post("/huespedes/actualizar/1")
                        .param("documento", "12345678")
                        .param("nombre", "Juan")
                        .param("apellido", "Perez")
                        .param("telefono", "3001112233")
                        .param("email", "juan@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/huespedes"))
                .andExpect(flash().attributeExists("mensaje"))
                .andReturn();
    }

    @Test
    void eliminar_exito_redirigeConFlash() throws Exception {
        when(huespedService.findById(1L)).thenReturn(
                new HuespedResponse(1L, "12345678", "Juan", "Perez", "3001112233", "juan@email.com")
        );
        doNothing().when(huespedService).delete(anyLong());

        MvcResult result = mockMvc.perform(post("/huespedes/eliminar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/huespedes"))
                .andExpect(flash().attributeExists("mensaje"))
                .andReturn();
    }
}