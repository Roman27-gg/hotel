package co.edu.ut.hotel.controller.web;

import co.edu.ut.hotel.service.HabitacionService;
import co.edu.ut.hotel.service.ReservaService;
import co.edu.ut.hotel.service.dto.response.HabitacionResponse;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de vista para TableroControllerWeb.
 */
@WebMvcTest(TableroControllerWeb.class)
@Import(TableroTestConfig.class)
class TableroControllerWebViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HabitacionService habitacionService;

    @MockBean
    private ReservaService reservaService;

    @Test
    void tablero_cargaCorrectamente() throws Exception {
        LocalDate hoy = LocalDate.of(2026, 11, 16);

        HabitacionResponse h1 = new HabitacionResponse(1L, "101", TipoHabitacion.SENCILLA, 2, BigDecimal.valueOf(80000), EstadoHabitacion.DISPONIBLE);
        HabitacionResponse h2 = new HabitacionResponse(2L, "102", TipoHabitacion.SENCILLA, 2, BigDecimal.valueOf(80000), EstadoHabitacion.RESERVADA);
        HabitacionResponse h3 = new HabitacionResponse(3L, "201", TipoHabitacion.SUITE, 4, BigDecimal.valueOf(200000), EstadoHabitacion.OCUPADA);
        HabitacionResponse h4 = new HabitacionResponse(4L, "202", TipoHabitacion.SUITE, 4, BigDecimal.valueOf(200000), EstadoHabitacion.MANTENIMIENTO);

        ReservaResponse r1 = new ReservaResponse(1L, "101", "Juan Perez", "12345678", hoy, hoy.plusDays(3), 3, 2, EstadoReserva.CONFIRMADA, BigDecimal.valueOf(240000), BigDecimal.ZERO, java.util.Map.of());
        ReservaResponse r2 = new ReservaResponse(2L, "102", "Maria Gomez", "87654321", hoy, hoy.plusDays(2), 2, 1, EstadoReserva.CONFIRMADA, BigDecimal.valueOf(160000), BigDecimal.ZERO, java.util.Map.of());

        when(habitacionService.findAll()).thenReturn(List.of(h1, h2, h3, h4));
        when(reservaService.findAll()).thenReturn(List.of(r1, r2));

        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("tablero"))
                .andExpect(model().attributeExists("disponibles", "reservadas", "ocupadas", "mantenimiento", "entranHoy", "salenHoy"))
                .andReturn();

        Document doc = PaginaAsserts.limpia(result.getResponse().getContentAsString(), "GET / tablero");
        // Verificar que los contadores aparecen
        var texto = doc.text();
        if (!texto.contains("Disponibles") || !texto.contains("Reservadas") || !texto.contains("Ocupadas") || !texto.contains("Mantenimiento")) {
            throw new AssertionError("Faltan contadores en tablero");
        }
    }
}