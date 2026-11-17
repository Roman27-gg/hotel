package co.edu.ut.hotel.controller.web;

import co.edu.ut.hotel.service.ReservaService;
import co.edu.ut.hotel.service.DisponibilidadService;
import co.edu.ut.hotel.service.CotizacionService;
import co.edu.ut.hotel.service.HuespedService;
import co.edu.ut.hotel.service.dto.request.ReservaRequest;
import co.edu.ut.hotel.service.dto.response.HabitacionDisponibleResponse;
import co.edu.ut.hotel.service.dto.response.HuespedResponse;
import co.edu.ut.hotel.service.dto.response.ReservaResponse;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.EstadoReserva;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de vista para ReservaControllerWeb (@WebMvcTest + Jsoup).
 * Valida validaciones por campo, mensajes visibles, valores conservados, selects recargados.
 */
@WebMvcTest(ReservaControllerWeb.class)
@Import(ThymeleafAutoConfiguration.class)
class ReservaControllerWebViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservaService reservaService;

    @MockBean
    private DisponibilidadService disponibilidadService;

    @MockBean
    private CotizacionService cotizacionService;

    @MockBean
    private HuespedService huespedService;

    @Test
    void nuevoForm_cargaCorrectamente() throws Exception {
        when(disponibilidadService.findAvailable(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(huespedService.findAll()).thenReturn(List.of());

        MvcResult result = mockMvc.perform(get("/reservas/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserva/form"))
                .andExpect(model().attributeExists("reserva", "habitaciones", "huespedes", "tipos"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET /reservas/nuevo");
    }

    @Test
    void guardar_fechaSalidaAntesDeEntrada_muestraErrorGlobal() throws Exception {
        when(disponibilidadService.findAvailable(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(huespedService.findAll()).thenReturn(List.of());

        MvcResult result = mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "1")
                        .param("huespedId", "1")
                        .param("fechaEntrada", "2026-12-25")
                        .param("fechaSalida", "2026-12-20") // anterior a entrada
                        .param("numHuespedes", "2"))
                .andExpect(status().isOk()) // MISMA vista, no redirect
                .andExpect(view().name("reserva/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "POST /reservas/guardar con salida < entrada");
        PaginaAsserts.errorGlobalVisible(doc, "debe mostrar error global @AssertTrue");
        // Verificar que se conservan los valores
        PaginaAsserts.dateInputValor(doc, "fechaEntrada", "2026-12-25", "fecha entrada conservada");
        PaginaAsserts.dateInputValor(doc, "fechaSalida", "2026-12-20", "fecha salida conservada");
    }

    @Test
    void guardar_fechaEntradaPasada_muestraErrorCampo() throws Exception {
        when(disponibilidadService.findAvailable(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(huespedService.findAll()).thenReturn(List.of());

        MvcResult result = mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "1")
                        .param("huespedId", "1")
                        .param("fechaEntrada", "2020-01-01") // pasada
                        .param("fechaSalida", "2020-01-05")
                        .param("numHuespedes", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserva/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "POST /reservas/guardar con fecha entrada pasada");
        PaginaAsserts.campoInvalidoConMensaje(doc, "fechaEntrada", "fecha entrada pasada debe mostrar error");
    }

    @Test
    void guardar_habitacionInexistente_muestraErrorAmigable() throws Exception {
        when(disponibilidadService.findAvailable(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(huespedService.findAll()).thenReturn(List.of());
        when(reservaService.create(any())).thenThrow(
                new co.edu.ut.hotel.service.exception.RecursoNoEncontradoException("Habitación no encontrada con id 999")
        );

        MvcResult result = mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "999")
                        .param("huespedId", "1")
                        .param("fechaEntrada", "2026-12-20")
                        .param("fechaSalida", "2026-12-25")
                        .param("numHuespedes", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserva/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "POST /reservas/guardar habitación inexistente");
        PaginaAsserts.errorGlobalVisible(doc, "debe mostrar error amigable de recurso no encontrado");
    }

    @Test
    void guardar_cruceFechas_muestraErrorAmigable() throws Exception {
        when(disponibilidadService.findAvailable(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(huespedService.findAll()).thenReturn(List.of());
        when(reservaService.create(any())).thenThrow(
                new co.edu.ut.hotel.service.exception.ReservaCruzadaException("La habitación 101 ya tiene una reserva confirmada en esas fechas")
        );

        MvcResult result = mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "1")
                        .param("huespedId", "1")
                        .param("fechaEntrada", "2026-12-20")
                        .param("fechaSalida", "2026-12-25")
                        .param("numHuespedes", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserva/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "POST /reservas/guardar cruce fechas");
        PaginaAsserts.errorGlobalVisible(doc, "debe mostrar error amigable de cruce");
    }

    @Test
    void guardar_exito_redirigeConFlash() throws Exception {
        when(reservaService.create(any())).thenReturn(
                new ReservaResponse(1L, 1L, 1L, "101", "Juan Perez", "12345678", LocalDate.now(), LocalDate.now().plusDays(3), 3, 2, EstadoReserva.CONFIRMADA, BigDecimal.valueOf(240000), BigDecimal.ZERO, java.util.Map.of())
        );

        MvcResult result = mockMvc.perform(post("/reservas/guardar")
                        .param("habitacionId", "1")
                        .param("huespedId", "1")
                        .param("fechaEntrada", "2026-12-20")
                        .param("fechaSalida", "2026-12-25")
                        .param("numHuespedes", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservas"))
                .andExpect(flash().attributeExists("mensaje"))
                .andReturn();
    }

    @Test
    void listar_conFiltros_funciona() throws Exception {
        when(reservaService.findAll()).thenReturn(List.of());

        MvcResult result = mockMvc.perform(get("/reservas")
                        .param("estado", "CONFIRMADA")
                        .param("documento", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserva/lista"))
                .andExpect(model().attributeExists("reservas", "estadoFiltro", "documentoFiltro"))
                .andReturn();

        PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET /reservas con filtros");
    }

    @Test
    void editarForm_cargaReservaConfirmada() throws Exception {
        ReservaResponse r = new ReservaResponse(1L, 1L, 1L, "101", "Juan Perez", "12345678",
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 3, 2, EstadoReserva.CONFIRMADA,
                BigDecimal.valueOf(240000), BigDecimal.ZERO, java.util.Map.of());
        when(reservaService.findById(1L)).thenReturn(r);
        when(disponibilidadService.findAllReservables()).thenReturn(List.of());
        when(huespedService.findAll()).thenReturn(List.of());

        MvcResult result = mockMvc.perform(get("/reservas/editar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserva/form"))
                .andExpect(model().attributeExists("reserva", "id", "habitaciones", "huespedes"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET /reservas/editar/1");
        // Verificar que se cargan los valores existentes
        var inputFechaEntrada = doc.select("input[name=fechaEntrada]").first();
        if (inputFechaEntrada == null || !"2026-11-20".equals(inputFechaEntrada.attr("value"))) {
            throw new AssertionError("Editar: fechaEntrada no se conserva");
        }
        var inputFechaSalida = doc.select("input[name=fechaSalida]").first();
        if (inputFechaSalida == null || !"2026-11-23".equals(inputFechaSalida.attr("value"))) {
            throw new AssertionError("Editar: fechaSalida no se conserva");
        }
    }

    @Test
    void editarForm_reservaNoEsConfirmada_redirige() throws Exception {
        ReservaResponse r = new ReservaResponse(1L, 1L, 1L, "101", "Juan Perez", "12345678",
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 3, 2, EstadoReserva.CANCELADA,
                BigDecimal.valueOf(240000), BigDecimal.ZERO, java.util.Map.of());
        when(reservaService.findById(1L)).thenReturn(r);

        MvcResult result = mockMvc.perform(get("/reservas/editar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservas"))
                .andExpect(flash().attributeExists("error"))
                .andReturn();
    }

    @Test
    void actualizar_exito_redirigeConFlash() throws Exception {
        ReservaResponse r = new ReservaResponse(1L, 1L, 1L, "101", "Juan Perez", "12345678",
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 3, 2, EstadoReserva.CONFIRMADA,
                BigDecimal.valueOf(240000), BigDecimal.ZERO, java.util.Map.of());
        when(reservaService.findById(1L)).thenReturn(r);
        when(reservaService.update(anyLong(), any())).thenReturn(r);
        when(disponibilidadService.findAllReservables()).thenReturn(List.of());
        when(huespedService.findAll()).thenReturn(List.of());

        MvcResult result = mockMvc.perform(post("/reservas/actualizar/1")
                        .param("habitacionId", "1")
                        .param("huespedId", "1")
                        .param("fechaEntrada", "2026-12-20")
                        .param("fechaSalida", "2026-12-25")
                        .param("numHuespedes", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservas"))
                .andExpect(flash().attributeExists("mensaje"))
                .andReturn();
    }

    @Test
    void actualizar_salidaAntesDeEntrada_muestraErrorGlobal() throws Exception {
        ReservaResponse r = new ReservaResponse(1L, 1L, 1L, "101", "Juan Perez", "12345678",
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 3, 2, EstadoReserva.CONFIRMADA,
                BigDecimal.valueOf(240000), BigDecimal.ZERO, java.util.Map.of());
        when(reservaService.findById(1L)).thenReturn(r);
        when(disponibilidadService.findAllReservables()).thenReturn(List.of());
        when(huespedService.findAll()).thenReturn(List.of());

        MvcResult result = mockMvc.perform(post("/reservas/actualizar/1")
                        .param("habitacionId", "1")
                        .param("huespedId", "1")
                        .param("fechaEntrada", "2026-12-25")
                        .param("fechaSalida", "2026-12-20")
                        .param("numHuespedes", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserva/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "POST /reservas/actualizar salida < entrada");
        PaginaAsserts.errorGlobalVisible(doc, "debe mostrar error global @AssertTrue");
    }

    @Test
    void actualizar_cruceFechas_muestraErrorAmigable() throws Exception {
        ReservaResponse r = new ReservaResponse(1L, 1L, 1L, "101", "Juan Perez", "12345678",
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 23), 3, 2, EstadoReserva.CONFIRMADA,
                BigDecimal.valueOf(240000), BigDecimal.ZERO, java.util.Map.of());
        when(reservaService.findById(1L)).thenReturn(r);
        when(reservaService.update(anyLong(), any())).thenThrow(
                new co.edu.ut.hotel.service.exception.ReservaCruzadaException("La habitación ya tiene una reserva confirmada en esas fechas")
        );
        when(disponibilidadService.findAllReservables()).thenReturn(List.of());
        when(huespedService.findAll()).thenReturn(List.of());

        MvcResult result = mockMvc.perform(post("/reservas/actualizar/1")
                        .param("habitacionId", "1")
                        .param("huespedId", "1")
                        .param("fechaEntrada", "2026-12-20")
                        .param("fechaSalida", "2026-12-25")
                        .param("numHuespedes", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserva/form"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "POST /reservas/actualizar cruce fechas");
        PaginaAsserts.errorGlobalVisible(doc, "debe mostrar error amigable de cruce");
    }
}