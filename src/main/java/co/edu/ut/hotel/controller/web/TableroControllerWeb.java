package co.edu.ut.hotel.controller.web;

import co.edu.ut.hotel.service.HabitacionService;
import co.edu.ut.hotel.service.ReservaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Controlador web del tablero de inicio.
 */
@Controller
public class TableroControllerWeb {

    private final HabitacionService habitacionService;
    private final ReservaService reservaService;
    private final Clock clock;

    public TableroControllerWeb(HabitacionService habitacionService, ReservaService reservaService, Clock clock) {
        this.habitacionService = habitacionService;
        this.reservaService = reservaService;
        this.clock = clock;
    }

    @GetMapping("/")
    public String tablero(Model model) {
        long disponibles = habitacionService.findAll().stream()
                .filter(h -> h.estado().toString().equals("DISPONIBLE")).count();
        long reservadas = habitacionService.findAll().stream()
                .filter(h -> h.estado().toString().equals("RESERVADA")).count();
        long ocupadas = habitacionService.findAll().stream()
                .filter(h -> h.estado().toString().equals("OCUPADA")).count();
        long mantenimiento = habitacionService.findAll().stream()
                .filter(h -> h.estado().toString().equals("MANTENIMIENTO")).count();

        LocalDate hoy = LocalDate.now(clock);
        long entranHoy = reservaService.findAll().stream()
                .filter(r -> r.estado().toString().equals("CONFIRMADA"))
                .filter(r -> r.fechaEntrada() != null && r.fechaEntrada().equals(hoy)).count();
        long salenHoy = reservaService.findAll().stream()
                .filter(r -> r.estado().toString().equals("CONFIRMADA"))
                .filter(r -> r.fechaSalida() != null && r.fechaSalida().equals(hoy)).count();

        model.addAttribute("disponibles", disponibles);
        model.addAttribute("reservadas", reservadas);
        model.addAttribute("ocupadas", ocupadas);
        model.addAttribute("mantenimiento", mantenimiento);
        model.addAttribute("entranHoy", entranHoy);
        model.addAttribute("salenHoy", salenHoy);
        return "tablero";
    }
}