# Plan para estabilizar la capa web Thymeleaf del proyecto hotel

## Resumen ejecutivo
La capa web (Spring MVC + Thymeleaf) tiene múltiples problemas que impiden el correcto funcionamiento: errores 500 al crear reservas, validaciones no mostradas, excepciones técnicas expuestas al usuario, y arquitectura inconsistente entre AGENTS.md y el código real.

---

## FASE 0 — Verificación inicial (COMPLETADA)
- ✅ Leído AGENTS.md, pom.xml, application.yml, db/init.sql
- ✅ Leídos todos los controladores en `controller/web`
- ✅ Leídas todas las plantillas en `resources/templates`
- ✅ Leídos DTOs de request/response, servicios impl, pruebas actuales

---

## FASE 1 — Reescribir AGENTS.md (fuente única de verdad)

### Problemas a corregir en AGENTS.md:
1. **Sección 3 (Stack)**: Dice "Sin JPA/Hibernate", "JDBC (JdbcTemplate)", "No agregar Spring Web ni Thymeleaf" → El código usa JPA + Spring Data JPA + Thymeleaf + Spring Web
2. **Sección 4 (Decisiones)**: Tabla dice "Spring Boot (sin web) + Java Swing", "DAO con JdbcTemplate", "Ventana principal (JFrame)", "view (Swing)", "controller registra listeners" → Código real: Thymeleaf, JpaRepository, @Controller web
3. **Sección 5 (Estructura)**: Paquetes `view/`, `NavegacionController`, `VentanaPrincipal`, `PanelBase`, `CardLayout`, EDT, prefijos btn/txt/cmb → No existen en código
4. **Sección 6.2 (Lombok)**: Reglas para Swing → Adaptar a DTOs web
5. **Sección 7 (BD)**: Puerto 5432 → Código usa 5434
6. **Sección 10 (Vista Swing)**: Completa → Reemplazar por "Vista Thymeleaf"
7. **Sección 13 (Comandos)**: `./mvnw spring-boot:run` abre ventana Swing → Arranca servidor web en 8081
8. **Sección 14 (Fuera de alcance)**: Check-in/out "fuera de alcance" → Ya implementados
9. **docker-compose.yml**: Comentario dice "La vista Swing requiere entorno gráfico" → Eliminar
10. **Añadir política de errores web** (ver task)

### Entregable: `AGENTS.md` reescrito completamente

---

## FASE 2 — Matriz de casos de prueba (`docs/matriz-casos-vistas.md`)

Crear matriz derivada de requisitos y reglas de negocio (NO del HTML actual). Una fila por caso: ID, pantalla, entrada, resultado esperado, test que lo cubre.

### Mínimos por formulario/listado:
- GET inicial (vacío y edición), lista vacía, un elemento, muchos; filtros y búsquedas con/sin resultados
- Cada anotación Bean Validation de cada Request DTO (un caso por campo + todos inválidos a la vez)
- Tras cualquier error: valores conservados (incluidas fechas) y selects recargados
- **Reserva**: salida <= entrada, entrada pasada, huéspedes > capacidad, habitación en mantenimiento, habitación/huésped inexistente, cruce (nuevo DENTRO de existente, nuevo CONTIENE existente, borde izquierdo, borde derecho, bordes exactos permitidos [entrada, salida)), cancelada que libera, doble envío, dos simultáneas (EXCLUDE BD)
- **Cancelación**: confirmada, ya cancelada, inexistente, con penalización
- **Check-in/out**: estados válidos e inválidos
- **Habitación**: número duplicado, eliminar con reservas, editar inexistente
- **Huésped**: documento duplicado, correo inválido
- **Recurso inexistente** (/reservas/999, /habitaciones/editar/999) → página amigable
- **Fallo inesperado** forzado → error.html sin traza
- **Caracteres especiales** escapados; sin `th:utext` salvo justificación
- **Mensajes flash** de éxito y que desaparecen al recargar

---

## FASE 3 — Tests (deben FALLAR donde hoy hay bugs)

### Dependencias a añadir en pom.xml (scope test):
```xml
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.18.1</version>
    <scope>test</scope>
</dependency>
```

### 1. `PaginaAsserts.limpia(html)` - helper en `src/test/java/co/edu/ut/hotel/test/`
Falla si contiene: "Whitelabel", "Exception", "org.springframework", "java.lang", "SQL", "constraint", "ex_reserva", "${", "[[", "th:", "Optional[" o parece JSON. Usar en TODA respuesta HTML.

### 2. `*ViewTest` (@WebMvcTest por controlador, servicios @MockitoBean, Jsoup)
- Validaciones por campo con mensaje visible junto al campo (`is-invalid` + texto)
- Valores conservados, selects recargados, vistas y atributos del modelo, escapado
- Archivos: `HabitacionControllerWebViewTest`, `HuespedControllerWebViewTest`, `ReservaControllerWebViewTest`, `DisponibilidadControllerWebViewTest`, `TableroControllerWebViewTest`

### 3. `*FlujoIT` (@SpringBootTest + @AutoConfigureMockMvc + Testcontainers)
- Flujos reales crear/cruzar/cancelar/check-in/check-out con BD real
- @ParameterizedTest de cruces con casos de la matriz
- Verifica status, vista, mensaje global visible, conteo de reservas NO cambió, `PaginaAsserts.limpia`
- Test que viole directamente la restricción EXCLUDE (saltando chequeo del servicio) → mensaje amigable

### 4. `RutasSmokeTest`
- Recorre `RequestMappingHandlerMapping`, todas las rutas GET sin variables → status < 500 y limpia
- Rutas con `{id}` con id existente e inexistente

### 5. Reglas de tests
- Fechas con `Clock` inyectado, nunca `LocalDate.now()`
- Tests nacen de la matriz, nunca copiando HTML actual
- PROHIBIDO borrar, comentar, @Disabled o debilitar un test

---

## FASE 4 — Correcciones de código (bucle iterativo)

### Hallazgos A–L (confirmados por lectura) + propios:

| ID | Hallazgo | Archivo(s) | Solución |
|----|----------|------------|----------|
| A | `GlobalExceptionHandler` es `@RestControllerAdvice` y devuelve JSON a controladores web | `GlobalExceptionHandler.java` | Cambiar a `@ControllerAdvice(basePackages = "co.edu.ut.hotel.controller.web")` y renderizar vistas HTML; mantener advice JSON para `@RestController` via `annotations = RestController.class` |
| B | Fragmento `alerts` recorre `${errores}` como Map pero controladores pasan `BindingResult` | `layout.html`, controladores | Eliminar parámetro `errores` de `alerts`; errores de validación se muestran dentro del form con `th:errorclass="is-invalid"` y `th:errors`; bloque de errores globales dentro del `th:object` |
| C | `<div class="invalid-feedback">` ocultos por Bootstrap (falta `th:errorclass="is-invalid"`) | Todas las plantillas form | Añadir `th:errorclass="is-invalid"` a inputs/selects; usar `th:errors` para mensaje |
| D | `ReservaRequest.isFechasValidas()` (@AssertTrue) genera error en campo `fechasValidas` que no se muestra | `reserva/form.html`, `ReservaRequest.java` | Mostrar error global dentro del form O convertir a error de campo `fechaSalida` con `@ScriptAssert` o validación en servicio |
| E | `catch (Exception e)` + `redirect:` + `e.getMessage()`: pierde datos y muestra mensajes crudos | Todos los controladores `guardar`/`actualizar` | Capturar excepciones específicas (`HotelException`, `DataIntegrityViolationException` constraint `ex_reserva_sin_cruce`, `IllegalArgumentException`); usar `errores.reject(codigo, mensajeAmigable)` y devolver MISMA vista con datos escritos; recargar combos con método `cargarCombos(model)` |
| F | `nuevoForm` y rama error de `guardar` solo cargan habitaciones libres para hoy→mañana | `ReservaControllerWeb.java` | Listar habitaciones reservables (no en MANTENIMIENTO) via `habitacionService.findAll()` filtrando estado; disponibilidad real la decide servicio al guardar |
| G | `#fields.hasGlobalErrors()` fuera del `th:object` en `reserva/form.html` | `reserva/form.html` | Mover bloque de errores globales DENTRO del `<form th:object="${reserva}">` |
| H | Sin `@DateTimeFormat(iso = DATE)` ni `spring.mvc.format.date=iso` | DTOs, `application.yml` | Añadir `@DateTimeFormat(iso = ISO.DATE)` a campos `LocalDate` en Request DTOs; `spring.mvc.format.date=iso` en application.yml |
| I | `disponibilidad/lista.html`: `th:href="'/reservas/nuevo?habitacionId='..."` sin `@{}` ni fechas | `disponibilidad/lista.html` | Usar `@{/reservas/nuevo(habitacionId=${h.id}, fechaEntrada=${request.fechaEntrada}, fechaSalida=${request.fechaSalida})}` y precargar en `nuevoForm` |
| J | `TableroControllerWeb` usa `LocalDate.now()` en vez de bean `Clock`; `HabitacionControllerWeb.actualizar` redirige a lista si falla | `TableroControllerWeb.java`, `HabitacionControllerWeb.java` | Inyectar `Clock` en TableroControllerWeb; en `actualizar` devolver misma vista con errores (no redirect) |
| K | AGENTS.md y docker-compose.yml hablan de Swing/JdbcTemplate | `AGENTS.md`, `docker-compose.yml` | FASE 1 |
| L | Buscar más problemas en plantillas habitacion, huesped, lista, detalle, tablero | Todas las plantillas | Revisar `th:errorclass`, `th:errors`, `th:field`, `th:object`, `@DateTimeFormat`, escapes |

### Correcciones adicionales por controlador:

#### `ReservaControllerWeb`
- `nuevoForm`: cargar TODAS las habitaciones no en MANTENIMIENTO (no solo disponibles para mañana)
- `guardar`: 
  - Si `errores.hasErrors()` → recargar combos + devolver misma vista (NO redirect)
  - Catch específico: `ReservaCruzadaException` → `errores.reject("fechaEntrada", "La habitación ya tiene una reserva confirmada en esas fechas")`
  - Catch `DataIntegrityViolationException` con constraint `ex_reserva_sin_cruce` → mismo mensaje
  - Catch `HotelException` → `errores.reject("global", ex.getMessage())`
  - Catch `IllegalArgumentException` del servicio → `errores.reject("global", ex.getMessage())`
- `cancelar`/`checkin`/`checkout`: catch específico → flash amigable + redirect

#### `HabitacionControllerWeb`
- `nuevoForm`: ya OK (añade `tipos`, `estados`)
- `guardar`/`actualizar`: mismo patrón que Reserva (catch específico, devolver vista con datos)
- `actualizar`: NO redirect si falla → devolver misma vista
- `eliminar`: catch `HabitacionConReservasException` → flash amigable

#### `HuespedControllerWeb`
- `guardar`: catch `IllegalArgumentException` (documento duplicado) → `errores.reject("documento", "Ya existe un huésped con ese documento")`

#### `DisponibilidadControllerWeb`
- Ya valida manualmente → OK, pero mejorar mensajes

#### `TableroControllerWeb`
- Inyectar `Clock` y usar `LocalDate.now(clock)`

### Correcciones en plantillas (todas):
- Añadir `th:errorclass="is-invalid"` a TODOS los inputs/selects con `th:field`
- Usar `th:errors="*{campo}"` en lugar de `th:each="err : ${#fields.errors('campo')}"` (más limpio)
- Añadir `@DateTimeFormat(iso = ISO.DATE)` a campos `LocalDate` en DTOs
- Bloque de errores globales DENTRO del `<form th:object="...">`
- Usar `@{...}` para todos los hrefs con parámetros
- Escapar outputs: `[[${...}]]` o `th:text`, nunca `th:utext`

### Correcciones en DTOs:
- `ReservaRequest`: `@DateTimeFormat(iso = ISO.DATE)` en `fechaEntrada`, `fechaSalida`
- `DisponibilidadRequest`: mismo
- `HabitacionRequest`: `@Max(6)` en `capacidad` (ya agregado)

### Correcciones en application.yml:
```yaml
spring:
  mvc:
    format:
      date: iso
server:
  error:
    whitelabel:
      enabled: false
    include-stacktrace: never
    include-message: never
```

---

## FASE 5 — Verificación final y entregables

### Bucle de iteración (máx 15):
1. `./mvnw -q test -Dtest='*ViewTest,*FlujoIT,RutasSmokeTest'`
2. Leer primer fallo → buscar causa raíz → corregir → repetir
3. Cada bug nuevo = test de regresión que falla ANTES de corregir
4. Actualizar matriz marcando lo cubierto
5. Terminar cuando pasen tests específicos Y `./mvnw test` completo (sin romper unitarias existentes)

### Entregables:
1. **Capturas de pantalla** (si Playwright disponible) en `docs/capturas/`:
   - Errores de validación (campo + global)
   - Cruce de fechas
   - Éxito creación/edición
   - Lista vacía / con datos
   - Página de error genérica
2. **`docs/plantilla-tabla-pruebas.md`** desde la matriz (ID, descripción, pasos, esperado, obtenido, captura)
3. **Resumen final**: archivos cambiados, bugs hallados con causa raíz, casos cubiertos, decisiones para sustentación

---

## Archivos a modificar (lista preliminar)

### Código principal:
- `AGENTS.md` (reescritura completa)
- `src/main/java/co/edu/ut/hotel/controller/exception/GlobalExceptionHandler.java`
- `src/main/java/co/edu/ut/hotel/controller/web/ReservaControllerWeb.java`
- `src/main/java/co/edu/ut/hotel/controller/web/HabitacionControllerWeb.java`
- `src/main/java/co/edu/ut/hotel/controller/web/HuespedControllerWeb.java`
- `src/main/java/co/edu/ut/hotel/controller/web/DisponibilidadControllerWeb.java`
- `src/main/java/co/edu/ut/hotel/controller/web/TableroControllerWeb.java`
- `src/main/java/co/edu/ut/hotel/service/dto/request/ReservaRequest.java`
- `src/main/java/co/edu/ut/hotel/service/dto/request/DisponibilidadRequest.java`
- `src/main/java/co/edu/ut/hotel/service/dto/request/HabitacionRequest.java`
- `src/main/resources/application.yml`
- `docker-compose.yml` (comentario)

### Plantillas:
- `src/main/resources/templates/fragments/layout.html` (quitar `errores` de `alerts`)
- `src/main/resources/templates/reserva/form.html`
- `src/main/resources/templates/reserva/lista.html`
- `src/main/resources/templates/reserva/detalle.html`
- `src/main/resources/templates/habitacion/form.html`
- `src/main/resources/templates/habitacion/lista.html`
- `src/main/resources/templates/huesped/form.html`
- `src/main/resources/templates/huesped/lista.html`
- `src/main/resources/templates/disponibilidad/lista.html`
- `src/main/resources/templates/tablero.html`
- `src/main/resources/templates/error.html` (nueva, genérica)

### Tests (nuevos):
- `pom.xml` (añadir jsoup)
- `src/test/java/co/edu/ut/hotel/test/PaginaAsserts.java`
- `src/test/java/co/edu/ut/hotel/controller/web/*ViewTest.java` (5 archivos)
- `src/test/java/co/edu/ut/hotel/controller/web/*FlujoIT.java` (5 archivos)
- `src/test/java/co/edu/ut/hotel/controller/web/RutasSmokeTest.java`

### Documentación:
- `docs/matriz-casos-vistas.md`
- `docs/plantilla-tabla-pruebas.md`
- `docs/capturas/` (si Playwright)

---

## Decisiones clave para la sustentación

1. **`@ControllerAdvice` vs `@RestControllerAdvice`**: Separar concerns - HTML para navegador, JSON para APIs
2. **Validación en controlador (BindingResult) vs `@Valid` + `@ExceptionHandler`**: BindingResult permite devolver misma vista con datos; ExceptionHandler JSON rompe UX web
3. **`th:errorclass="is-invalid"` + `th:errors`**: Bootstrap 5 requiere clase `is-invalid` en el input para mostrar feedback
4. **`@DateTimeFormat(iso = DATE)`**: Enlace bidireccional correcto para `<input type="date">`
4. **Recargar combos en error**: PRG pattern roto intencionalmente para UX (datos no se pierden)
5. **Catch específico vs `Exception`**: Mensajes amigables requieren distinguir tipos de error
6. **Clock inyectado vs `LocalDate.now()`**: Testabilidad determinista
7. **Restricción EXCLUDE en BD + chequeo en servicio**: Defensa en profundidad (race conditions)
8. **AGENTS.md como fuente única**: Documentación viva que refleja código real

---

## Próximos pasos inmediatos

1. **Reescribir AGENTS.md** completamente (FASE 1)
2. **Crear matriz de casos** en `docs/matriz-casos-vistas.md` (FASE 2)
3. **Añadir jsoup a pom.xml** y crear `PaginaAsserts` (FASE 3)
4. **Escribir tests que FALLEN** para cada hallazgo A-L (FASE 3)
5. **Ejecutar bucle de corrección** (FASE 4)