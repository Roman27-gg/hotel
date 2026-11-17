package co.edu.ut.hotel.controller.web;

import co.edu.ut.hotel.service.HuespedService;
import co.edu.ut.hotel.service.dto.request.HuespedRequest;
import co.edu.ut.hotel.service.dto.response.HuespedResponse;
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
 * Controlador web de huespedes (CRUD completo).
 */
@Controller
@RequestMapping("/huespedes")
public class HuespedControllerWeb {

    private final HuespedService servicio;

    public HuespedControllerWeb(HuespedService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model model, @RequestParam(required = false) String buscar) {
        List<HuespedResponse> lista = servicio.findAll();
        if (buscar != null && !buscar.isEmpty()) {
            lista = lista.stream()
                    .filter(h -> h.documento().contains(buscar) || h.getNombreCompleto().toLowerCase().contains(buscar.toLowerCase()))
                    .toList();
        }
        model.addAttribute("huespedes", lista);
        model.addAttribute("buscar", buscar);
        return "huesped/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(@ModelAttribute("huesped") HuespedRequest request, Model model) {
        model.addAttribute("id", null);
        return "huesped/form";
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            HuespedResponse existente = servicio.findById(id);
            HuespedRequest req = new HuespedRequest();
            req.setDocumento(existente.documento());
            req.setNombre(existente.nombre());
            req.setApellido(existente.apellido());
            req.setTelefono(existente.telefono());
            req.setEmail(existente.email());
            model.addAttribute("huesped", req);
            model.addAttribute("id", id);
            return "huesped/form";
        } catch (RecursoNoEncontradoException ex) {
            redirect.addFlashAttribute("error", "El huesped no existe");
            return "redirect:/huespedes";
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "No se pudo cargar el huesped");
            return "redirect:/huespedes";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("huesped") HuespedRequest request,
                          BindingResult errores, Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            model.addAttribute("id", null);
            return "huesped/form";
        }
        try {
            servicio.create(request);
            redirect.addFlashAttribute("mensaje", "Huesped guardado correctamente");
            return "redirect:/huespedes";
        } catch (HotelException ex) {
            errores.reject("global", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            model.addAttribute("id", null);
            return "huesped/form";
        } catch (DataIntegrityViolationException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("huesped_documento_key")) {
                errores.reject("documento", "Ya existe un huesped con ese documento");
                model.addAttribute("errorGlobal", "Ya existe un huesped con ese documento");
            } else {
                errores.reject("global", "No se pudo completar la operacion con la base de datos. Intente de nuevo.");
                model.addAttribute("errorGlobal", "No se pudo completar la operacion con la base de datos. Intente de nuevo.");
            }
            model.addAttribute("id", null);
            return "huesped/form";
        } catch (IllegalArgumentException ex) {
            errores.reject("global", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            model.addAttribute("id", null);
            return "huesped/form";
        } catch (Exception ex) {
            errores.reject("global", "Ocurrio un error inesperado. Intente de nuevo.");
            model.addAttribute("errorGlobal", "Ocurrio un error inesperado. Intente de nuevo.");
            model.addAttribute("id", null);
            return "huesped/form";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("huesped") HuespedRequest request,
                             BindingResult errores, Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            model.addAttribute("id", id);
            return "huesped/form";
        }
        try {
            servicio.update(id, request);
            redirect.addFlashAttribute("mensaje", "Huesped actualizado correctamente");
            return "redirect:/huespedes";
        } catch (HotelException ex) {
            errores.reject("global", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            model.addAttribute("id", id);
            return "huesped/form";
        } catch (DataIntegrityViolationException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("huesped_documento_key")) {
                errores.reject("documento", "Ya existe un huesped con ese documento");
                model.addAttribute("errorGlobal", "Ya existe un huesped con ese documento");
            } else {
                errores.reject("global", "No se pudo completar la operacion con la base de datos. Intente de nuevo.");
                model.addAttribute("errorGlobal", "No se pudo completar la operacion con la base de datos. Intente de nuevo.");
            }
            model.addAttribute("id", id);
            return "huesped/form";
        } catch (IllegalArgumentException ex) {
            errores.reject("global", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            model.addAttribute("id", id);
            return "huesped/form";
        } catch (Exception ex) {
            errores.reject("global", "Ocurrio un error inesperado. Intente de nuevo.");
            model.addAttribute("errorGlobal", "Ocurrio un error inesperado. Intente de nuevo.");
            model.addAttribute("id", id);
            return "huesped/form";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            servicio.delete(id);
            redirect.addFlashAttribute("mensaje", "Huesped eliminado correctamente");
        } catch (RecursoNoEncontradoException ex) {
            redirect.addFlashAttribute("error", "El huesped no existe");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "No se pudo eliminar el huesped");
        }
        return "redirect:/huespedes";
    }
}