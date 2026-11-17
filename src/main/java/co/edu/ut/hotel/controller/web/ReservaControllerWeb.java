package co.edu.ut.hotel.controller.web;

import co.edu.ut.hotel.service.CotizacionService;
import co.edu.ut.hotel.service.DisponibilidadService;
import co.edu.ut.hotel.service.HuespedService;
import co.edu.ut.hotel.service.ReservaService;
import co.edu.ut.hotel.service.dto.request.ReservaRequest;
import co.edu.ut.hotel.service.dto.response.HabitacionDisponibleResponse;
import co.edu.ut.hotel.service.dto.response.HuespedResponse;
import co.edu.ut.hotel.service.dto.response.ReservaResponse;
import co.edu.ut.hotel.service.enums.EstadoHabitacion;
import co.edu.ut.hotel.service.enums.EstadoReserva;
import co.edu.ut.hotel.service.exception.HotelException;
import co.edu.ut.hotel.service.exception.RecursoNoEncontradoException;
import co.edu.ut.hotel.service.exception.ReservaCruzadaException;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador web de reservas: creacion (transaccional), consulta, cancelacion, check-in y check-out.
 */
@Controller
@RequestMapping("/reservas")
public class ReservaControllerWeb {

    private final ReservaService servicio;
    private final DisponibilidadService disponibilidadService;
    private final CotizacionService cotizacionService;
    private final HuespedService huespedService;

    public ReservaControllerWeb(ReservaService servicio, DisponibilidadService disponibilidadService,
                                CotizacionService cotizacionService, HuespedService huespedService) {
        this.servicio = servicio;
        this.disponibilidadService = disponibilidadService;
        this.cotizacionService = cotizacionService;
        this.huespedService = huespedService;
    }

    private void cargarCombos(Model model, LocalDate fechaEntrada, LocalDate fechaSalida) {
        List<HabitacionDisponibleResponse> habitaciones;
        if (fechaEntrada != null && fechaSalida != null) {
            habitaciones = disponibilidadService.findAvailable(fechaEntrada, fechaSalida, null, null);
        } else {
            // Cargar todas las habitaciones que NO estan en mantenimiento (reservables)
            habitaciones = disponibilidadService.findAllReservables();
        }
        model.addAttribute("habitaciones", habitaciones);
        model.addAttribute("huespedes", huespedService.findAll());
        model.addAttribute("tipos", new String[]{"SENCILLA", "SUITE"});
    }

    @GetMapping
    public String listar(Model model,
                         @RequestParam(required = false) String estado,
                         @RequestParam(required = false) String documento) {
        List<ReservaResponse> lista = servicio.findAll();
        if ((estado == null || estado.isEmpty()) && (documento == null || documento.isEmpty())) {
            model.addAttribute("reservas", lista);
        } else {
            lista = lista.stream()
                    .filter(r -> (estado == null || estado.isEmpty() || r.estado().toString().equals(estado)))
                    .filter(r -> documento == null || documento.isEmpty()
                            || r.huespedDocumento().toLowerCase().contains(documento.toLowerCase()))
                    .toList();
            model.addAttribute("reservas", lista);
        }
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("documentoFiltro", documento);
        return "reserva/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(@ModelAttribute("reserva") ReservaRequest request, Model model) {
        cargarCombos(model, null, null);
        model.addAttribute("reserva", request);
        return "reserva/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("reserva") ReservaRequest request,
                          BindingResult errores, Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            model.addAttribute("errorGlobal", "Corrija los errores en el formulario");
            cargarCombos(model, request.getFechaEntrada(), request.getFechaSalida());
            return "reserva/form";
        }
        try {
            servicio.create(request);
            redirect.addFlashAttribute("mensaje", "Reserva creada correctamente");
            return "redirect:/reservas";
        } catch (ReservaCruzadaException ex) {
            errores.reject("fechaEntrada", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            cargarCombos(model, request.getFechaEntrada(), request.getFechaSalida());
            return "reserva/form";
        } catch (RecursoNoEncontradoException ex) {
            errores.reject("habitacionId", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            cargarCombos(model, request.getFechaEntrada(), request.getFechaSalida());
            return "reserva/form";
        } catch (HotelException ex) {
            errores.reject("global", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            cargarCombos(model, request.getFechaEntrada(), request.getFechaSalida());
            return "reserva/form";
        } catch (DataIntegrityViolationException ex) {
            // Constraint EXCLUDE de la BD (race condition)
            if (ex.getMessage() != null && ex.getMessage().contains("ex_reserva_sin_cruce")) {
                errores.reject("fechaEntrada", "La habitación ya tiene una reserva confirmada en esas fechas");
                model.addAttribute("errorGlobal", "La habitación ya tiene una reserva confirmada en esas fechas");
            } else {
                errores.reject("global", "No se pudo completar la operación con la base de datos. Intente de nuevo.");
                model.addAttribute("errorGlobal", "No se pudo completar la operación con la base de datos. Intente de nuevo.");
            }
            cargarCombos(model, request.getFechaEntrada(), request.getFechaSalida());
            return "reserva/form";
        } catch (IllegalArgumentException ex) {
            errores.reject("global", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            cargarCombos(model, request.getFechaEntrada(), request.getFechaSalida());
            return "reserva/form";
        } catch (Exception ex) {
            errores.reject("global", "Ocurrió un error inesperado. Intente de nuevo.");
            model.addAttribute("errorGlobal", "Ocurrió un error inesperado. Intente de nuevo.");
            cargarCombos(model, request.getFechaEntrada(), request.getFechaSalida());
            return "reserva/form";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            ReservaResponse r = servicio.findById(id);
            model.addAttribute("reserva", r);
            return "reserva/detalle";
        } catch (RecursoNoEncontradoException ex) {
            redirect.addFlashAttribute("error", "La reserva no existe");
            return "redirect:/reservas";
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "No se pudo cargar el detalle de la reserva");
            return "redirect:/reservas";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            ReservaResponse existente = servicio.findById(id);
            ReservaRequest req = new ReservaRequest();
            req.setHabitacionId(existente.habitacionId());
            req.setHuespedId(existente.huespedId());
            req.setFechaEntrada(existente.fechaEntrada());
            req.setFechaSalida(existente.fechaSalida());
            req.setNumHuespedes(existente.numHuespedes());
            req.setEstado(existente.estado());
            model.addAttribute("reserva", req);
            model.addAttribute("id", id);
            model.addAttribute("habitacionNumero", existente.habitacionNumero());
            model.addAttribute("huespedNombreCompleto", existente.huespedNombreCompleto());
            model.addAttribute("habitaciones", disponibilidadService.findAllReservables());
            model.addAttribute("huespedes", huespedService.findAll());
            return "reserva/form";
        } catch (RecursoNoEncontradoException ex) {
            redirect.addFlashAttribute("error", "La reserva no existe");
            return "redirect:/reservas";
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "No se pudo cargar la reserva");
            return "redirect:/reservas";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("reserva") ReservaRequest request,
                             BindingResult errores, Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            model.addAttribute("id", id);
            model.addAttribute("errorGlobal", "Corrija los errores en el formulario");
            model.addAttribute("habitaciones", disponibilidadService.findAllReservables());
            model.addAttribute("huespedes", huespedService.findAll());
            return "reserva/form";
        }
        try {
            servicio.update(id, request);
            redirect.addFlashAttribute("mensaje", "Reserva actualizada correctamente");
            return "redirect:/reservas";
        } catch (RecursoNoEncontradoException ex) {
            errores.reject("habitacionId", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            model.addAttribute("id", id);
            model.addAttribute("habitaciones", disponibilidadService.findAllReservables());
            model.addAttribute("huespedes", huespedService.findAll());
            return "reserva/form";
        } catch (ReservaCruzadaException ex) {
            errores.reject("fechaEntrada", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            model.addAttribute("id", id);
            model.addAttribute("habitaciones", disponibilidadService.findAllReservables());
            model.addAttribute("huespedes", huespedService.findAll());
            return "reserva/form";
        } catch (HotelException ex) {
            errores.reject("global", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            model.addAttribute("id", id);
            model.addAttribute("habitaciones", disponibilidadService.findAllReservables());
            model.addAttribute("huespedes", huespedService.findAll());
            return "reserva/form";
        } catch (DataIntegrityViolationException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("ex_reserva_sin_cruce")) {
                errores.reject("fechaEntrada", "La habitación ya tiene una reserva confirmada en esas fechas");
                model.addAttribute("errorGlobal", "La habitación ya tiene una reserva confirmada en esas fechas");
            } else {
                errores.reject("global", "No se pudo completar la operación con la base de datos. Intente de nuevo.");
                model.addAttribute("errorGlobal", "No se pudo completar la operación con la base de datos. Intente de nuevo.");
            }
            model.addAttribute("id", id);
            model.addAttribute("habitaciones", disponibilidadService.findAllReservables());
            model.addAttribute("huespedes", huespedService.findAll());
            return "reserva/form";
        } catch (IllegalArgumentException ex) {
            errores.reject("global", ex.getMessage());
            model.addAttribute("errorGlobal", ex.getMessage());
            model.addAttribute("id", id);
            model.addAttribute("habitaciones", disponibilidadService.findAllReservables());
            model.addAttribute("huespedes", huespedService.findAll());
            return "reserva/form";
        } catch (Exception ex) {
            errores.reject("global", "Ocurrió un error inesperado. Intente de nuevo.");
            model.addAttribute("errorGlobal", "Ocurrió un error inesperado. Intente de nuevo.");
            model.addAttribute("id", id);
            model.addAttribute("habitaciones", disponibilidadService.findAllReservables());
            model.addAttribute("huespedes", huespedService.findAll());
            return "reserva/form";
        }
    }

    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            servicio.cancel(id);
            redirect.addFlashAttribute("mensaje", "Reserva cancelada correctamente");
        } catch (co.edu.ut.hotel.service.exception.CancelacionNoPermitidaException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (RecursoNoEncontradoException ex) {
            redirect.addFlashAttribute("error", "La reserva no existe");
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "No se pudo cancelar la reserva");
        }
        return "redirect:/reservas";
    }

    @PostMapping("/checkin/{id}")
    public String checkIn(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            servicio.checkIn(id);
            redirect.addFlashAttribute("mensaje", "Check-in realizado correctamente");
        } catch (RecursoNoEncontradoException ex) {
            redirect.addFlashAttribute("error", "La reserva no existe");
        } catch (HotelException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "No se pudo realizar el check-in");
        }
        return "redirect:/reservas";
    }

    @PostMapping("/checkout/{id}")
    public String checkOut(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            servicio.checkOut(id);
            redirect.addFlashAttribute("mensaje", "Check-out realizado correctamente");
        } catch (RecursoNoEncontradoException ex) {
            redirect.addFlashAttribute("error", "La reserva no existe");
        } catch (HotelException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "No se pudo realizar el check-out");
        }
        return "redirect:/reservas";
    }
}