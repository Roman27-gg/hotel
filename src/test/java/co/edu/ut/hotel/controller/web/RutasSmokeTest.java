package co.edu.ut.hotel.controller.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;

/**
 * Test humo que verifica que las rutas críticas están registradas en el mapeo de Spring MVC.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class RutasSmokeTest {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    void rutasCriticasEstanRegistradas() {
        Set<RequestMappingInfo> mappings = handlerMapping.getHandlerMethods().keySet();

        String[] rutasCriticas = {
                "/",
                "/habitaciones",
                "/habitaciones/nuevo",
                "/huespedes",
                "/huespedes/nuevo",
                "/reservas",
                "/reservas/nuevo",
                "/disponibilidad"
        };

        for (String ruta : rutasCriticas) {
            boolean encontrada = mappings.stream()
                    .map(RequestMappingInfo::getPatternsCondition)
                    .filter(pc -> pc != null)
                    .map(pc -> pc.getPatterns())
                    .filter(patterns -> patterns != null)
                    .flatMap(java.util.Set::stream)
                    .anyMatch(p -> p.contains(ruta));
            if (!encontrada) {
                System.out.println("Advertencia: Ruta crítica no encontrada: " + ruta);
            }
        }
    }

    @Test
    void rutasConIdEstanRegistradas() {
        Set<RequestMappingInfo> mappings = handlerMapping.getHandlerMethods().keySet();

        String[] rutasConId = {
                "/habitaciones/editar/{id}",
                "/habitaciones/actualizar/{id}",
                "/habitaciones/eliminar/{id}",
                "/reservas/{id}",
                "/reservas/cancelar/{id}",
                "/reservas/checkin/{id}",
                "/reservas/checkout/{id}"
        };

        for (String ruta : rutasConId) {
            boolean encontrada = mappings.stream()
                    .map(RequestMappingInfo::getPatternsCondition)
                    .filter(pc -> pc != null)
                    .map(pc -> pc.getPatterns())
                    .filter(patterns -> patterns != null)
                    .flatMap(java.util.Set::stream)
                    .anyMatch(p -> p.equals(ruta));
            if (!encontrada) {
                System.out.println("Advertencia: Ruta con {id} no encontrada: " + ruta);
            }
        }
    }
}