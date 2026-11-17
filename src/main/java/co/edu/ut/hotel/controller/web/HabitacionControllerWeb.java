package co.edu.ut.hotel.controller.web;

import co.edu.ut.hotel.service.HabitacionService;
import co.edu.ut.hotel.service.dto.request.HabitacionRequest;
import co.edu.ut.hotel.service.dto.response.HabitacionResponse;
import co.edu.ut.hotel.service.exception.HabitacionConReservasException;
import co.edu.ut.hotel.service.exception.HotelException;
import co.edu.ut.hotel.service.exception.RecursoNoEncontradoException;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador web de habitaciones (CRUD completo).
 */
@Controller
@RequestMapping("/habitaciones")
public class HabitacionControllerWeb {

    private final HabitacionService servicio;

    public HabitacionControllerWeb(HabitacionService servicio) {
        this.servicio = servicio;
    }

    private void cargarCombos(Model model) {
        model.addAttribute("tipos", new String[]{"SENCILLA", "SUITE"});
        model.addAttribute("estados", new String[]{"DISPONIBLE", "RESERVADA", "OCUPADA", "MANTENIMIENTO"});
    }

    @GetMapping
    public String listar(Model model,
                         @RequestParam(required = false) String tipo,
                         @RequestParam(required = false) String estado) {
        List<HabitacionResponse> lista;
        if ((tipo == null || tipo.isEmpty()) && (estado == null || estado.isEmpty())) {
            lista = servicio.findAll();
        } else {
            lista = servicio.findByTypeAndState(tipo, estado);
        }
        model.addAttribute("habitaciones", lista);
        model.addAttribute("tipoFiltro", tipo);
        model.addAttribute("estadoFiltro", estado);
        return "habitacion/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(@ModelAttribute("habitacion") HabitacionRequest request, Model model) {
        cargarCombos(model);
        return "habitacion/form";
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            HabitacionResponse existente = servicio.findById(id);
            HabitacionRequest req = new HabitacionRequest();
            req.setNumero(existente.numero());
            req.setTipo(existente.tipo().name());
            req.setCapacidad(existente.capacidad());
            req.setTarifaNoche(existente.tarifaNoche());
            req.setEstado(existente.estado().name());
            model.addAttribute("habitacion", req);
            model.addAttribute("id", id);
            cargarCombos(model);
            return "habitacion/form";
        } catch (RecursoNoEncontradoException ex) {
            redirect.addFlashAttribute("error", "La habitación no existe");
            return "redirect:/habitaciones";
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "No se pudo cargar la habitación");
            return "redirect:/habitaciones";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("habitacion") HabitacionRequest request,
                          BindingResult errores, Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            cargarCombos(model);
            return "habitacion/form";
        }
        try {
            servicio.create(request);
            redirect.addFlashAttribute("mensaje", "Habitacion guardada correctamente");
            return "redirect:/habitaciones";
        } catch (HotelException ex) {
            errores.reject("global", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            cargarCombos(model);
            return "habitacion/form";
        } catch (DataIntegrityViolationException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("habitacion_numero_key")) {
                errores.reject("numero", "Ya existe una habitación con ese número");
                model.addAttribute("errorGlobal", "Ya existe una habitación con ese número");
            } else {
                errores.reject("global", "No se pudo completar la operación con la base de datos. Intente de nuevo.");
                model.addAttribute("errorGlobal", "No se pudo completar la operación con la base de datos. Intente de nuevo.");
            }
            cargarCombos(model);
            return "habitacion/form";
        } catch (IllegalArgumentException ex) {
            errores.reject("global", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            cargarCombos(model);
            return "habitacion/form";
        } catch (Exception ex) {
            errores.reject("global", "Ocurrió un error inesperado. Intente de nuevo.");
            model.addAttribute("errorGlobal", "Ocurrió un error inesperado. Intente de nuevo.");
            cargarCombos(model);
            return "habitacion/form";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("habitacion") HabitacionRequest request,
                             BindingResult errores, Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            model.addAttribute("id", id);
            cargarCombos(model);
            return "habitacion/form";
        }
        try {
            servicio.update(id, request);
            redirect.addFlashAttribute("mensaje", "Habitacion actualizada correctamente");
            return "redirect:/habitaciones";
        } catch (HotelException ex) {
            errores.reject("global", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            model.addAttribute("id", id);
            cargarCombos(model);
            return "habitacion/form";
        } catch (DataIntegrityViolationException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("habitacion_numero_key")) {
                errores.reject("numero", "Ya existe una habitación con ese número");
                model.addAttribute("errorGlobal", "Ya existe una habitación con ese número");
            } else {
                errores.reject("global", "No se pudo completar la operación con la base de datos. Intente de nuevo.");
                model.addAttribute("errorGlobal", "No se pudo completar la operación con la base de datos. Intente de nuevo.");
            }
            model.addAttribute("id", id);
            cargarCombos(model);
            return "habitacion/form";
        } catch (IllegalArgumentException ex) {
            errores.reject("global", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            model.addAttribute("id", id);
            cargarCombos(model);
            return "habitacion/form";
        } catch (Exception ex) {
            errores.reject("global", "Ocurrió un error inesperado. Intente de nuevo.");
            model.addAttribute("errorGlobal", "Ocurrió un error inesperado. Intente de nuevo.");
            model.addAttribute("id", id);
            cargarCombos(model);
            return "habitacion/form";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            servicio.delete(id);
            redirect.addFlashAttribute("mensaje", "Habitacion eliminada correctamente");
        } catch (HabitacionConReservasException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (RecursoNoEncontradoException ex) {
            redirect.addFlashAttribute("error", "La habitación no existe");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "No se pudo eliminar la habitación");
        }
        return "redirect:/habitaciones";
    }
}