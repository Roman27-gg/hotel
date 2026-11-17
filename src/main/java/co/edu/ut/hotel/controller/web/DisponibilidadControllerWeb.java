package co.edu.ut.hotel.controller.web;

import co.edu.ut.hotel.service.DisponibilidadService;
import co.edu.ut.hotel.service.dto.request.DisponibilidadRequest;
import co.edu.ut.hotel.service.dto.response.HabitacionDisponibleResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controlador web de disponibilidad: consulta habitaciones libres para un rango de fechas.
 * Las validaciones de formato (fecha salida > entrada, etc.) se hacen con Jakarta Bean Validation
 * en {@link DisponibilidadRequest}. Las reglas de negocio (cruce de fechas, mantenimiento)
 * se validan en {@link DisponibilidadService}.
 */
@Controller
@RequestMapping("/disponibilidad")
public class DisponibilidadControllerWeb {

    private final DisponibilidadService servicio;

    public DisponibilidadControllerWeb(DisponibilidadService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String consultar(Model model,
                            @ModelAttribute("request") DisponibilidadRequest request) {
        List<HabitacionDisponibleResponse> lista = null;
        String mensaje = "";
        String error = null;

        // Validar manualmente para GET
        boolean tieneErrores = false;
        if (request.getFechaEntrada() == null) {
            error = "La fecha de entrada es obligatoria";
            tieneErrores = true;
        } else if (request.getFechaSalida() == null) {
            error = "La fecha de salida es obligatoria";
            tieneErrores = true;
        } else if (!request.getFechaSalida().isAfter(request.getFechaEntrada())) {
            error = "La fecha de salida debe ser posterior a la de entrada";
            tieneErrores = true;
        }

        if (tieneErrores) {
            model.addAttribute("disponibles", lista);
            model.addAttribute("error", error);
            model.addAttribute("tipos", new String[]{"SENCILLA", "SUITE"});
            return "disponibilidad/lista";
        }

        lista = servicio.findAvailable(
                request.getFechaEntrada(), request.getFechaSalida(),
                request.getTipo(), request.getPersonas());
        if (lista.isEmpty()) {
            mensaje = "No hay habitaciones disponibles para esas fechas";
        }
        model.addAttribute("disponibles", lista);
        model.addAttribute("mensaje", mensaje);
        model.addAttribute("tipos", new String[]{"SENCILLA", "SUITE"});
        return "disponibilidad/lista";
    }
}