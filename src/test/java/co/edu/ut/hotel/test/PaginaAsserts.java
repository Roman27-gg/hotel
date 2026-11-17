package co.edu.ut.hotel.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.regex.Pattern;

/**
 * Utilidad para aserciones sobre páginas HTML renderizadas por Thymeleaf.
 * Falla si la respuesta contiene trazas técnicas, JSON, errores de plantilla, etc.
 */
public final class PaginaAsserts {

    private static final Pattern JSON_PATTERN = Pattern.compile("^\\s*\\{.*\\}\\s*$", Pattern.DOTALL);

    private PaginaAsserts() {
    }

    /**
     * Valida que el HTML está limpio: sin Whitelabel, sin trazas, sin SQL, sin expresiones Thymeleaf sin procesar.
     *
     * @param html contenido HTML de la respuesta
     * @param contexto descripción de qué se está probando (para mensajes de error claros)
     * @return Document de Jsoup para encadenar más aserciones
     */
    public static Document limpia(String html, String contexto) {
        if (html == null || html.isBlank()) {
            throw new AssertionError(contexto + ": respuesta vacía o nula");
        }

        String lower = html.toLowerCase();

        // Whitelabel Error Page de Spring Boot
        if (lower.contains("whitelabel error page")) {
            throw new AssertionError(contexto + ": contiene 'Whitelabel Error Page' (error 500 sin manejar)");
        }

        // Traza de excepción Java/Spring
        if (lower.contains("org.springframework") || lower.contains("java.lang.exception") || lower.contains("java.lang.error")) {
            throw new AssertionError(contexto + ": contiene traza de excepción (org.springframework/java.lang)");
        }

        // SQL o constraints expuestos
        if (lower.contains("constraint") || lower.contains("ex_reserva") || lower.contains("sql") || lower.contains("psql") || lower.contains("postgresql")) {
            throw new AssertionError(contexto + ": contiene SQL/constraint expuesto al usuario");
        }

        // Optional[...] sin procesar
        if (html.contains("Optional[")) {
            throw new AssertionError(contexto + ": contiene 'Optional[' sin procesar");
        }

        // Parece JSON en lugar de HTML
        if (JSON_PATTERN.matcher(html.trim()).matches()) {
            throw new AssertionError(contexto + ": la respuesta parece JSON, se esperaba HTML");
        }

        // Parsear con Jsoup para validar estructura
        Document doc = Jsoup.parse(html);
        if (doc.select("html").isEmpty()) {
            throw new AssertionError(contexto + ": no es HTML válido (falta etiqueta <html>)");
        }

        return doc;
    }

    /**
     * Valida que un campo tiene la clase is-invalid (Bootstrap 5) y muestra mensaje de error.
     *
     * @param doc       documento parseado
     * @param fieldName nombre del campo (ej: "fechaEntrada")
     * @param contexto  descripción de la prueba
     */
    public static void campoInvalidoConMensaje(Document doc, String fieldName, String contexto) {
        String selector = "[name=" + fieldName + "].is-invalid, [id=" + fieldName + "].is-invalid, [th\\:field=\"*" + fieldName + "\"].is-invalid";
        var elementos = doc.select(selector);
        if (elementos.isEmpty()) {
            // Buscar por th:field
            elementos = doc.select("[th\\:field=\"*" + fieldName + "\"]");
            if (elementos.isEmpty()) {
                throw new AssertionError(contexto + ": campo '" + fieldName + "' no encontrado en el formulario");
            }
            if (!elementos.first().hasClass("is-invalid")) {
                throw new AssertionError(contexto + ": campo '" + fieldName + "' debería tener clase 'is-invalid' pero no la tiene");
            }
        }

        // Verificar que hay mensaje de error (invalid-feedback o th:errors)
        var feedback = doc.select(".invalid-feedback");
        boolean hayMensaje = feedback.stream()
                .anyMatch(el -> el.text().trim().length() > 0);
        if (!hayMensaje) {
            // Buscar th:errors
            var thErrors = doc.select("[th\\:errors]");
            hayMensaje = thErrors.stream()
                    .anyMatch(el -> el.text().trim().length() > 0);
        }
        if (!hayMensaje) {
            throw new AssertionError(contexto + ": campo '" + fieldName + "' inválido pero no hay mensaje de error visible");
        }
    }

    /**
     * Valida que NO hay campos con is-invalid (formulario válido).
     *
     * @param doc      documento parseado
     * @param contexto descripción de la prueba
     */
    public static void sinCamposInvalidos(Document doc, String contexto) {
        var invalidos = doc.select(".is-invalid");
        if (!invalidos.isEmpty()) {
            String campos = invalidos.stream()
                    .map(el -> el.attr("name").isEmpty() ? el.attr("id") : el.attr("name"))
                    .filter(s -> !s.isEmpty())
                    .toList()
                    .toString();
            throw new AssertionError(contexto + ": hay campos con 'is-invalid' cuando no debería: " + campos);
        }
    }

    /**
     * Valida que hay un bloque de error global (alert-danger) con mensaje.
     *
     * @param doc      documento parseado
     * @param contexto descripción de la prueba
     */
    public static void errorGlobalVisible(Document doc, String contexto) {
        var alerts = doc.select(".alert-danger, .alert-warning");
        if (alerts.isEmpty()) {
            throw new AssertionError(contexto + ": se esperaba un error global visible (alert-danger/alert-warning) pero no hay ninguno");
        }
        boolean conTexto = alerts.stream().anyMatch(el -> el.text().trim().length() > 0);
        if (!conTexto) {
            throw new AssertionError(contexto + ": hay alerta de error pero sin texto");
        }
    }

    /**
     * Valida que un select tiene las opciones esperadas.
     *
     * @param doc           documento parseado
     * @param selectName    name del select
     * @param expectedValues valores que deben estar presentes
     * @param contexto      descripción de la prueba
     */
    public static void selectConOpciones(Document doc, String selectName, String[] expectedValues, String contexto) {
        var select = doc.select("select[name=" + selectName + "]").first();
        if (select == null) {
            throw new AssertionError(contexto + ": select '" + selectName + "' no encontrado");
        }
        var opciones = select.select("option");
        for (String val : expectedValues) {
            boolean encontrado = opciones.stream()
                    .anyMatch(opt -> val.equals(opt.attr("value")) || val.equals(opt.text().trim()));
            if (!encontrado) {
                throw new AssertionError(contexto + ": select '" + selectName + "' no tiene la opción '" + val + "'");
            }
        }
    }

    /**
     * Valida que un input type="date" tiene el valor esperado (formato yyyy-MM-dd).
     *
     * @param doc        documento parseado
     * @param fieldName  nombre del campo
     * @param expected   valor esperado (puede ser null para vacío)
     * @param contexto   descripción de la prueba
     */
    public static void dateInputValor(Document doc, String fieldName, String expected, String contexto) {
        var input = doc.select("input[name=" + fieldName + "][type=date]").first();
        if (input == null) {
            // Buscar por th:field
            input = doc.select("[th\\:field=\"*" + fieldName + "\"][type=date]").first();
        }
        if (input == null) {
            throw new AssertionError(contexto + ": input date '" + fieldName + "' no encontrado");
        }
        String valor = input.attr("value");
        if (expected == null) {
            if (!valor.isEmpty()) {
                throw new AssertionError(contexto + ": input date '" + fieldName + "' debería estar vacío pero vale '" + valor + "'");
            }
        } else if (!expected.equals(valor)) {
            throw new AssertionError(contexto + ": input date '" + fieldName + "' esperaba '" + expected + "' pero vale '" + valor + "'");
        }
    }

    /**
     * Valida que un enlace usa @{...} (Thymeleaf link) y no URL hardcodeada.
     *
     * @param doc      documento parseado
     * @param href     href esperado (patrón)
     * @param contexto descripción de la prueba
     */
    public static void enlaceConThymeleaf(Document doc, String href, String contexto) {
        var enlaces = doc.select("a[href]");
        boolean encontrado = enlaces.stream()
                .anyMatch(a -> a.attr("href").contains(href));
        if (!encontrado) {
            throw new AssertionError(contexto + ": no hay enlace con href que contenga '" + href + "'");
        }
    }
}