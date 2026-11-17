# AGENTS.md — Caso 5: Hotel pequeño, reservas de habitaciones

> Guía para agentes de código (y para el equipo). Léela completa antes de escribir código.
> Origen: *Convocatoria Uno*, Profundización de Programación Orientada a Objetos 1, Universidad del Tolima.
> **Entrega del informe: 18 de noviembre, 18:59. Sustentación individual: 18 de noviembre, 19:00.**

---

## 1. Contexto del caso

Un hotel familiar de **20 habitaciones** recibe reservas por teléfono y mensajes. A veces vende la misma habitación a dos clientes para las mismas fechas.

- **Necesidad:** gestionar habitaciones, huéspedes y reservas **sin cruces de fechas**.
- **Entidades sugeridas:** Habitación, Huésped, Reserva.
- **Transacción obligatoria:** crear una reserva verificando que la habitación esté libre en el rango de fechas y cambiando su estado. Si hay cruce, **no se guarda nada**.
- **Funcionalidades extra pedidas ("Para pensar"):**
  1. ¿Cómo se calcula el valor total de la estadía?
  2. ¿Cómo se maneja la cancelación de una reserva?
- **Usuarios de la aplicación:** recepcionista (uso diario) y administrador/dueño (gestiona habitaciones y tarifas). No hay login en el alcance (ver sección 14).

## 2. Reglas del juego (lo que más importa)

1. **La nota es individual y sale de la sustentación.** Cada integrante (máx. 3) debe poder explicar cualquier parte, ejecutar el proyecto desde cero, provocar un rollback y **hacer un cambio pequeño en vivo** (agregar un campo, una validación o un filtro).
2. **Por eso: código simple, explícito y comentado.** Una solución sencilla y bien justificada vale más que una grande que nadie puede explicar.
   - **Se usa Lombok** para quitar código repetitivo, pero cada integrante debe saber explicar qué genera cada anotación y qué escribiría a mano sin ella. Como el encapsulamiento se evalúa, **las reglas de validación del dominio se escriben a mano** en constructores y setters de las entidades (Lombok no genera un setter si ya existe uno escrito). Ver sección 6.2.
   - **JPA + Spring Data JPA** (`JpaRepository`) con consultas `@Query` en JPQL. Consultas parametrizadas, sin concatenación de texto de usuario en SQL.
   - Sin abstracciones "mágicas" ni patrones que no se puedan justificar en una frase.
3. **Nunca concatenar texto del usuario en el SQL.** Siempre `?` o parámetros nombrados.
4. **Arquitectura MVC estricta.** `view` (plantillas Thymeleaf en `src/main/resources/templates`) solo muestra y captura datos; `controller` (en `controller/web`) conecta la vista con el servicio; `service` es el **modelo** (entidades, reglas de negocio, DTOs, mappers, repositorios y excepciones, cada uno en su subcarpeta **dentro** de `service`). La vista nunca llama a un servicio ni ejecuta SQL; el controlador nunca ejecuta SQL; las reglas de negocio viven en el servicio o en las entidades, nunca en la vista. El controlador **solo recibe y devuelve DTOs**: las entidades no llegan a la vista ni los DTOs al repositorio; el **mapper** es el único que convierte entre ambos.
5. **El informe es individual y con las palabras de cada estudiante.** Informes idénticos o copiados sin citar = nota mínima. Por eso el agente **no redacta el informe**: solo genera insumos (diagramas, tablas, scripts, listas de pruebas) en `docs/`. Ver sección 15.
6. Cada decisión técnica debe poder **justificarse frente a la alternativa** (sección 4).

## 3. Stack fijado

| Pieza | Valor |
|---|---|
| Lenguaje | **Java 21** (LTS) |
| Framework | **Spring Boot 3.4.x** (estable, p. ej. 3.4.4). **Spring Boot 4.x no existe** al momento de escribir esto; no usar RC/milestones. Se usa **con servidor web** (Tomcat embebido): aporta inyección de dependencias, JPA/Hibernate, transacciones y validación. |
| Vista | **Spring Boot Web MVC + Thymeleaf** (plantillas HTML en `src/main/resources/templates`). **Spring Boot 3.4.x** aporta el servidor Tomcat embebido. No se usa Swing. |
| Acceso a datos | **JPA + Spring Data JPA** (`JpaRepository`) con consultas `@Query` en JPQL. Consultas parametrizadas, sin concatenación de texto de usuario en SQL. |
| Base de datos | **PostgreSQL 17**, levantada con **Docker Compose** |
| Driver | `org.postgresql:postgresql` (scope runtime) |
| Reducción de código repetitivo | **Lombok** (versión gestionada por Spring Boot) |
| Validación | **Jakarta Bean Validation** (spring-boot-starter-validation) en los DTOs de entrada, ejecutada con `@Valid` desde los controladores web; más validaciones propias en las entidades |
| Manejo de errores | **`@ControllerAdvice`** (`GlobalExceptionHandler` en `controller/exception`) para controladores web: renderiza vistas HTML con mensajes amigables; **`@RestControllerAdvice`** (mismo archivo, `annotations = RestController.class`) para APIs REST: devuelve **JSON** con `ErrorResponseDTO` (`codigoDeError`, `descripcion`, `hora`, `path`, `detalles`) y códigos HTTP adecuados (404, 400, 409, 500). |
| Conversión entidad ↔ DTO | **Mappers manuales** (`@Component`, paquete `service/mapper`) |
| Build | Maven (con wrapper `./mvnw`) |
| Pruebas | JUnit 5 + Testcontainers (PostgreSQL) para la prueba de rollback |

Notas para el arranque y dependencias:
- Desde la 4.0 los starters están modularizados. Genera el `pom.xml` desde **start.spring.io** (Java 21, Maven, Jar) y no copies nombres de starters de memoria. Dependencias a pedir: **Spring Web**, **Thymeleaf**, **Spring Data JPA**, **Validation**, **PostgreSQL Driver**, **Lombok** y, opcional, **Testcontainers**.
- Si un artefacto no resuelve, revisa el nombre exacto en Initializr antes de improvisar.
- Lombok va como dependencia `optional`/de compilación (no debe empaquetarse en el JAR) y el IDE necesita el plugin y "annotation processing" activado.
- Spring Boot arranca como aplicación web (Tomcat embebido en el puerto 8081). Se accede vía HTTP en `http://localhost:8081` (mapeado en docker-compose).
- No hace falta entorno gráfico ni `headless(false)`.
- Los DTOs de entrada y salida son clases con Lombok (no `record`), por consistencia con el resto del proyecto.

Paquete base: `co.edu.ut.hotel` — groupId `co.edu.ut`, artifactId `hotel`.

## 4. Decisiones técnicas y su justificación

El informe y la sustentación piden justificar cada decisión frente a las alternativas. Esta es la base; cada integrante debe poder decirla con sus palabras.

| Decisión | Elegido | Alternativa | Por qué |
|---|---|---|---|
| Tecnología de presentación | **Spring Boot Web MVC + Thymeleaf** | JSF + Facelets + GlassFish, o Spring Boot (sin web) + Java Swing | Aplicación web estándar: se accede por navegador, plantillas HTML servidas por HTTP, controladores reciben peticiones (no eventos de Swing). Spring Boot aporta Tomcat embebido, inyección de dependencias, JPA/Hibernate, transacciones y validación. Con Swing habría que mantener UI de escritorio, EDT, CardLayout, y no hay navegador. |
| Capa de datos | **JPA + Spring Data JPA** (`JpaRepository`) | DAO con `JdbcTemplate` o JDBC puro | Menos código repetitivo (CRUD automático, paginación, `@Query` JPQL). Se integra con `@Transactional`: Spring gestiona la transacción y el `EntityManager`. Con `JdbcTemplate` habría que escribir RowMappers, SQL manual y pasar `Connection` para transacciones multi-tabla. |
| Base de datos | **PostgreSQL** | MySQL (XAMPP) | Restricción `EXCLUDE USING gist` sobre rangos de fechas: **la propia BD impide el cruce de reservas** aunque dos recepcionistas reserven a la vez (race condition). Docker Compose hace el entorno reproducible. |
| Organización MVC | **Tres paquetes MVC:** `controller/web` (C), `service` (M), plantillas Thymeleaf en `resources/templates` (V), y **dentro de `service`** las subcarpetas `entity`, `enums`, `dto`, `mapper`, `repository`, `exception` e `impl` | Todo mezclado, o entidades/DTO fuera del servicio | El modelo queda autocontenido: la vista y el controlador solo conocen sus interfaces y DTOs. La vista pinta, el controlador coordina, el servicio tiene reglas y transacciones, el repositorio solo JPQL/SQL, las entidades datos y reglas propias, y el mapper aísla la conversión. |
| Transacciones | **`@Transactional` en el método del servicio** | `commit/rollback` manual en el repositorio | La transacción es una unidad de negocio ("crear reserva"), no una operación de tabla. Rollback automático ante `RuntimeException`. |
| Interfaz | **Plantillas Thymeleaf** con layout común (`fragments/layout.html`), Bootstrap 5 por CDN | JSF, Vaadin, o Swing | Una sola plantilla base (head, navbar, alerts, scripts). Thymeleaf es estándar en Spring Boot, sintaxis cercana a HTML, fácil de depurar. Bootstrap da UI consistente sin CSS custom. |
| Entidad ↔ DTO | **DTOs de entrada y salida + mapper manual** | Pasar la entidad directo a la vista, o MapStruct | La vista no depende del modelo interno; cambiar una entidad no rompe las plantillas. El mapper manual se puede leer y explicar línea por línea, y es el punto natural donde se elige la subclase (`HabitacionSencilla`/`HabitacionSuite`); MapStruct agrega un procesador de anotaciones que además debe convivir con Lombok. |
| Validación y errores | **Bean Validation en DTOs + validación en entidades + `@ControllerAdvice` web / `@RestControllerAdvice` API** | `if` sueltos en cada plantilla | Cada capa valida lo suyo con mensajes claros. Controlador web: `@Valid` + `BindingResult` → devuelve MISMA vista con datos escritos y errores junto a campos (`th:errorclass="is-invalid"` + `th:errors`). Excepciones de negocio (`HotelException` y subclases) → `errores.reject(codigo, mensajeAmigable)` y misma vista. `@RestControllerAdvice` solo para `@RestController` (JSON). |
| Lombok | **Sí** (con las reglas de la sección 6.2) | Getters/setters a mano | Menos ruido en entidades, DTOs e inyección por constructor; las reglas del dominio siguen escritas a mano para poder explicarlas. |

## 5. Estructura del proyecto

MVC con tres capas: **`controller/web`** (C), **`service`** (M), **plantillas Thymeleaf** en `resources/templates` (V). Todo lo del modelo vive **dentro de `service`**, en subcarpetas.

Reglas de dependencia (se revisan en cada commit):
- Plantillas (V) → solo `service.dto` y `service.enums` (para mostrar y recoger datos). **No** importan servicios, repositorios ni entidades, y no conocen a los controladores.
- `controller/web` → plantillas, interfaces de `service`, `service.dto` y `service.exception`.
- `service.impl` → `service.repository`, `service.mapper`, `service.entity`, `service.exception`.
- Nada dentro de `service` importa `controller` ni plantillas.
- `controller/web` y las interfaces de `service` hablan con **DTOs**; `service.impl` y `service.repository` hablan con **entidades**; el **mapper** es el único que convierte.

```
hotel/
├── AGENTS.md
├── docker-compose.yml
├── pom.xml
├── db/
│   └── init.sql                          # script SQL (anexo del informe); Docker lo ejecuta al crear el volumen
├── docs/                                 # insumos para el informe (diagramas, tablas) — sección 15
├── src/main/java/co/edu/ut/hotel/
│   ├── HotelApplication.java             # arranca Spring (web, Tomcat embebido en 8081)
│   ├── config/                           # ClockConfig y demás configuración
│   │
│   ├── controller/
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler.java   # @ControllerAdvice (web) + @RestControllerAdvice (API)
│   │   └── web/                            # Controladores web (@Controller, @RequestMapping)
│   │       ├── TableroControllerWeb.java
│   │       ├── HabitacionControllerWeb.java
│   │       ├── HuespedControllerWeb.java
│   │       ├── ReservaControllerWeb.java
│   │       └── DisponibilidadControllerWeb.java
│   │
│   └── service/                          # M — todo el modelo, separado en subcarpetas
│       ├── HabitacionService.java        # interfaces (el contrato que ve el controlador)
│       ├── HuespedService.java
│       ├── ReservaService.java
│       ├── DisponibilidadService.java    # consulta de habitaciones libres y cotizaciones
│       ├── CotizacionService.java        # calculo de valor de estadia con desglose
│       ├── impl/                         # reglas de negocio y @Transactional
│       │   ├── HabitacionServiceImpl.java
│       │   ├── HuespedServiceImpl.java
│       │   ├── ReservaServiceImpl.java   # aqui vive la transaccion obligatoria
│       │   ├── DisponibilidadServiceImpl.java
│       │   └── CotizacionServiceImpl.java
│       ├── exception/                    # HotelException + subclases + SimulacionFalloException
│       │   ├── HotelException.java                   # base (extiende RuntimeException)
│       │   ├── ReservaCruzadaException.java          # dispara el rollback obligatorio
│       │   ├── HabitacionNoDisponibleException.java
│       │   ├── CancelacionNoPermitidaException.java
│       │   ├── HabitacionConReservasException.java
│       │   ├── RecursoNoEncontradoException.java
│       │   └── SimulacionFalloException.java         # solo efecto en perfil demo
│       ├── entity/                       # dominio POO: clases con reglas propias (sin SQL, sin Thymeleaf)
│       │   ├── Habitacion.java           # abstracta
│       │   ├── HabitacionSencilla.java
│       │   ├── HabitacionSuite.java
│       │   ├── Huesped.java
│       │   └── Reserva.java
│       ├── enums/
│       │   ├── TipoHabitacion.java
│       │   ├── EstadoHabitacion.java
│       │   └── EstadoReserva.java
│       ├── dto/
│       │   ├── request/                  # datos que arma la vista (con Bean Validation)
│       │   │   ├── HabitacionRequest.java
│       │   │   ├── HuespedRequest.java
│       │   │   ├── ReservaRequest.java
│       │   │   └── DisponibilidadRequest.java
│       │   └── response/                 # datos listos para mostrar en la vista
│       │       ├── HabitacionResponse.java
│       │       ├── HuespedResponse.java
│       │       ├── ReservaResponse.java          # incluye el desglose del valor
│       │       ├── CotizacionResponse.java       # noches + Map<String, BigDecimal> con el desglose
│       │       └── HabitacionDisponibleResponse.java   # habitación + valor total estimado
│       ├── mapper/                       # conversión entidad ↔ DTO (sin reglas de negocio)
│       │   ├── HabitacionMapper.java
│       │   ├── HuespedMapper.java
│       │   └── ReservaMapper.java
│       ├── repository/                   # solo JPQL/SQL (Spring Data JPA)
│       │   ├── HabitacionDao.java        # extiende JpaRepository + consultas propias
│       │   ├── HuespedDao.java
│       │   └── ReservaDao.java
│       └── util/                         # validaciones y cálculos de dominio reutilizables
│           ├── HabitacionUtil.java
│           ├── HuespedUtil.java
│           └── ReservaUtil.java
├── src/main/resources/
│   ├── application.yml
│   └── application-demo.yml              # perfil para demostrar el rollback
├── src/main/resources/templates/
│   ├── fragments/
│   │   └── layout.html                   # layout común: head, navbar, alerts, scripts
│   ├── tablero.html
│   ├── habitacion/
│   │   ├── lista.html
│   │   └── form.html
│   ├── huesped/
│   │   ├── lista.html
│   │   └── form.html
│   ├── reserva/
│   │   ├── lista.html
│   │   ├── form.html
│   │   └── detalle.html
│   └── disponibilidad/
│       └── lista.html
└── src/test/java/co/edu/ut/hotel/        # pruebas unitarias (entity, mapper, validador, handler) e integración (service/repository)
```

Convención de nombres: `XRequest` / `XResponse` para DTOs, `XMapper`, `XService` + `XServiceImpl`, `XDao` (extends `JpaRepository`), `XControllerWeb` para controladores.

> Nota para el informe: en el MVC del documento, **Modelo = paquete `service`** (entidades, reglas y acceso a datos), **Vista = plantillas Thymeleaf** y **Controlador = `controller/web`**. Si el docente prefiere que el paquete se llame literalmente `model`, basta renombrar `service` → `model` (refactor de un paso en el IDE).

## 6. Dominio y POO (qué se pide y dónde se cumple)

Todos estos puntos se preguntan en la sustentación: *dónde lo apliqué, por qué y qué problema habría si lo quitara*.

| # | Concepto | Dónde está en este proyecto | Qué problema resuelve |
|---|---|---|---|
| 1 | **Encapsulamiento** (≥4 clases) | Paquete `service/entity`: `Habitacion`, `HabitacionSencilla`, `HabitacionSuite`, `Huesped`, `Reserva`: atributos `private`; constructores/getters/setters generados por Lombok; **validaciones escritas a mano** en constructores y setters de la propia clase | Ningún objeto queda en estado inválido (tarifa negativa, salida antes de la entrada) sin importar quién lo cree |
| 2 | **Herencia** | `Habitacion` (abstracta) → `HabitacionSencilla`, `HabitacionSuite` | Atributos y comportamiento comunes en un solo lugar; tipos con reglas distintas |
| 3 | **Polimorfismo** | `calcularValorEstadia(int noches)` y `desglosarValor(int noches)` sobrescritos con `@Override`; el servicio y las vistas llaman al método sobre `Habitacion` sin saber el tipo | Sin él habría un `if (tipo == SUITE)` repetido en servicio, controlador y plantilla |
| 4 | **Interfaces / abstracción** | `JpaRepository` / `HabitacionDao` etc. implementados por Spring Data; `XService` interfaces | El servicio depende de la interfaz; se puede cambiar o simular el repositorio (pruebas) sin tocar el servicio |
| 5 | **DAO + capa de servicio** | `service/repository` solo JPQL; `service` + `service/impl` reglas y transacciones; `service/mapper` convierte entidad ↔ DTO; `controller/web` solo coordina vista y servicio con DTOs; plantillas solo Thymeleaf | Separación de responsabilidades; el controlador jamás ejecuta SQL/JPQL |
| 6 | **Excepciones propias** | `ReservaCruzadaException` (y las demás) lanzadas en la regla de negocio; provocan el rollback; `GlobalExceptionHandler` las convierte en mensajes para el usuario (web) o JSON (API) | Mensajes claros para el usuario y rollback automático |
| 7 | **Colecciones y genéricos** | `List<Habitacion>`, `List<Reserva>`, `Map<String, BigDecimal>` para el desglose del valor (subtotal, recargo, descuento, total) | Resultados tipados y detalle de la transacción ordenado (`LinkedHashMap`) |
| 8 | **Enumeraciones** | `EstadoHabitacion`, `EstadoReserva`, `TipoHabitacion` | Estados cerrados y seguros, sin "strings mágicos" |
| 9 | **Documentación** | Javadoc en todas las clases de `entity`, en las interfaces de `service` y en `JpaRepository`; **diagrama de clases UML** en `docs/` | Se entiende el diseño sin leer todo el código |

### 6.1 Entidades (diseño)

Todas viven en `service/entity`; los enums, en `service/enums`.

**`Habitacion` (abstracta)**: `id`, `numero` (String, único), `capacidad` (1–6), `tarifaNoche` (`BigDecimal` > 0), `estado` (`EstadoHabitacion`).
- Método abstracto: `BigDecimal calcularValorEstadia(int noches)`.
- Método abstracto/hook: `TipoHabitacion getTipo()`.
- Método concreto: `Map<String, BigDecimal> desglosarValor(int noches)` (usa `calcularValorEstadia` y las piezas de cada subclase; devuelve `LinkedHashMap`).
- Método concreto: `boolean admite(int personas)`; `boolean estaEnMantenimiento()`.

**`HabitacionSencilla`**: `calcularValorEstadia` = `tarifaNoche × noches`.

**`HabitacionSuite`**: `calcularValorEstadia` = `tarifaNoche × noches` + **recargo de servicios 15 %**, y **descuento 10 % desde 5 noches**. Constantes con nombre (`RECARGO_SERVICIOS`, `NOCHES_PARA_DESCUENTO`, `DESCUENTO_ESTADIA_LARGA`), no números sueltos.

**`Huesped`**: `id`, `documento` (único, solo dígitos/letras, 5–20), `nombre`, `apellido`, `telefono`, `email` (opcional, formato válido), `fechaRegistro`.

**`Reserva`**: `id`, `habitacion` (`Habitacion`), `huesped` (`Huesped`), `fechaEntrada`, `fechaSalida`, `numHuespedes`, `estado` (`EstadoReserva`), `valorTotal`, `penalizacion`, `fechaCreacion`, `fechaCancelacion`.
- Validaciones propias: salida posterior a entrada; entrada no anterior a hoy (al crear); `numHuespedes` entre 1 y capacidad.
- `int getNoches()`; `BigDecimal calcularPenalizacion(LocalDate hoy)` (política de cancelación, sección 8.2).

**Enums**
- `TipoHabitacion { SENCILLA, SUITE }`
- `EstadoHabitacion { DISPONIBLE, RESERVADA, OCUPADA, MANTENIMIENTO }`
- `EstadoReserva { CONFIRMADA, CANCELADA, FINALIZADA }`

**Mapeo JPA → objetos:** una sola tabla `habitacion` con columna `tipo` (discriminador). JPA construye `HabitacionSencilla` o `HabitacionSuite` según `tipo` (herencia `SINGLE_TABLE` con `@DiscriminatorColumn`).

### 6.2 Lombok: qué se usa, dónde y qué NO

| Dónde | Anotaciones permitidas | Notas |
|---|---|---|
| **Entidades** | `@Getter`, `@Setter`, `@NoArgsConstructor` (lo usa JPA), `@AllArgsConstructor` en las que no tienen reglas especiales | Si un campo tiene regla (tarifa > 0, capacidad 1–6, fechas coherentes, documento válido), **se escribe el setter y/o el constructor a mano** con la validación y lanzando una excepción con mensaje claro. Lombok omite el setter generado cuando ya existe uno escrito. |
| **Subclases** (`HabitacionSencilla`, `HabitacionSuite`) | `@NoArgsConstructor` y un constructor escrito a mano que llame a `super(...)` | `@AllArgsConstructor` solo incluye los campos propios de la clase, no los heredados. |
| **DTOs de entrada** | `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` | Más las anotaciones de validación (6.3). |
| **DTOs de salida** | `@Getter`, `@Builder`, `@AllArgsConstructor` | Inmutables en la práctica. |
| **Controladores, servicios, repositorios, mappers** | `@RequiredArgsConstructor` con campos `private final` (inyección por constructor) y `@Slf4j` para logs | Reemplaza `@Autowired` en campos y `LoggerFactory`. |

**No usar:** `@Data` en entidades (con herencia genera `equals/hashCode` engañosos), `@SneakyThrows`, `@Value`, `val`/`var` de Lombok.

**Para la sustentación:** cada integrante debe poder decir qué código genera cada anotación y mostrarlo (opción *Delombok* del IDE o `javap -p` sobre la clase compilada). El docente evalúa encapsulamiento: atributos privados, constructores, getters/setters y validaciones dentro de la clase. Lombok genera lo mismo que escribirías a mano, pero hay que saber explicarlo.

### 6.3 DTOs y mappers

**DTOs de entrada (`service/dto/request`)** — los arma la vista Thymeleaf (binding automático `th:field="*{campo}"`) y llevan las anotaciones de Bean Validation (`@NotBlank`, `@Size`, `@Email`, `@Positive`, `@DecimalMin`, `@NotNull`, `@FutureOrPresent`, `@DateTimeFormat(iso = ISO.DATE)`...). Ejemplos de contenido:
- `HabitacionRequest`: `numero`, `tipo`, `capacidad`, `tarifaNoche`, `estado`.
- `HuespedRequest`: `documento`, `nombre`, `apellido`, `telefono`, `email`.
- `ReservaRequest`: `habitacionId`, `huespedId`, `fechaEntrada`, `fechaSalida`, `numHuespedes`, `simularFallo` (solo perfil demo).
- `DisponibilidadRequest`: `fechaEntrada`, `fechaSalida`, `tipo` (opcional), `personas` (opcional).

**DTOs de salida (`service/dto/response`)** — planos y listos para la vista: `HabitacionResponse` (`id`, `numero`, `tipo`, `capacidad`, `tarifaNoche`, `estado`), `ReservaResponse` (`id`, `habitacionNumero`, `huespedNombreCompleto`, `huespedDocumento`, fechas, `noches`, `estado`, `valorTotal`, `penalizacion`, `desglose` como `Map<String, BigDecimal>`), `CotizacionResponse`, `HabitacionDisponibleResponse`.

**Mappers (`service/mapper`)** — clases `@Component`, sin SQL ni reglas de negocio:
- `toEntity(Request)` y `toResponse(Entity)`; `toResponseList(List<Entity>)`; `actualizarEntidad(Entity, Request)` para editar.
- `HabitacionMapper.toEntity` decide la subclase según `tipo` (`SENCILLA` → `HabitacionSencilla`, `SUITE` → `HabitacionSuite`).
- `ReservaMapper.toEntity(request, habitacion, huesped)`: el servicio carga la habitación y el huésped con el repositorio y se los pasa. `ReservaMapper.toResponse` llama al método polimórfico `desglosarValor` de la habitación.

**Flujo de una petición (para explicar en la sustentación):**
Petición HTTP del navegador → **Controlador web** (toma el `Request` del binding, lo valida con `@Valid` + `BindingResult`) → `ServiceImpl` (mapper → entidad, reglas, `@Transactional`) → `Repository` (JPQL parametrizado) → BD → entidad → `Mapper` → `Response` → **Controlador** → **Plantilla Thymeleaf** (`model.addAttribute(...)`, `th:text`, `th:field`) → HTML → navegador.

## 7. Base de datos (PostgreSQL)

### 7.1 `docker-compose.yml`

```yaml
services:
  db:
    image: postgres:17-alpine
    container_name: hotel-db
    environment:
      POSTGRES_DB: hotel
      POSTGRES_USER: hotel
      POSTGRES_PASSWORD: hotel
    ports:
      - "5434:5432"            # si el puerto está ocupado, usar "5435:5432" y ajustar application.yml
    volumes:
      - hotel_pgdata:/var/lib/postgresql/data
      - ./db/init.sql:/docker-entrypoint-initdb.d/01-init.sql:ro
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U hotel -d hotel"]
      interval: 5s
      timeout: 3s
      retries: 10

volumes:
  hotel_pgdata:
```

- Levantar: `docker compose up -d` — Detener: `docker compose down`.
- **Reiniciar la BD desde cero (re-ejecuta `init.sql`):** `docker compose down -v && docker compose up -d`. El script solo corre cuando el volumen es nuevo.

### 7.2 `application.yml`

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5434/hotel}
    username: ${DB_USER:hotel}
    password: ${DB_PASSWORD:hotel}
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    cache: false
  main:
    banner-mode: off
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: none
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        jdbc:
          lob:
            non_contextual_creation: true
  mvc:
    format:
      date: iso
server:
  port: 8081
  error:
    whitelabel:
      enabled: false
    include-stacktrace: never
    include-message: never
hotel:
  demo:
    simular-fallo-habilitado: false
```

### 7.3 `db/init.sql` (esquema base — el agente puede completarlo con datos de ejemplo)

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE huesped (
    id             BIGSERIAL PRIMARY KEY,
    documento      VARCHAR(20)  NOT NULL UNIQUE,
    nombre         VARCHAR(60)  NOT NULL,
    apellido       VARCHAR(60)  NOT NULL,
    telefono       VARCHAR(20)  NOT NULL,
    email          VARCHAR(120),
    fecha_registro TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE habitacion (
    id           BIGSERIAL PRIMARY KEY,
    numero       VARCHAR(10)   NOT NULL UNIQUE,
    tipo         VARCHAR(20)   NOT NULL CHECK (tipo IN ('SENCILLA','SUITE')),
    capacidad    SMALLINT      NOT NULL CHECK (capacidad BETWEEN 1 AND 6),
    tarifa_noche NUMERIC(10,2) NOT NULL CHECK (tarifa_noche > 0),
    estado       VARCHAR(20)   NOT NULL DEFAULT 'DISPONIBLE'
                 CHECK (estado IN ('DISPONIBLE','RESERVADA','OCUPADA','MANTENIMIENTO'))
);

CREATE TABLE reserva (
    id                BIGSERIAL PRIMARY KEY,
    habitacion_id     BIGINT        NOT NULL REFERENCES habitacion(id) ON DELETE RESTRICT,
    huesped_id        BIGINT        NOT NULL REFERENCES huesped(id)    ON DELETE RESTRICT,
    fecha_entrada     DATE          NOT NULL,
    fecha_salida      DATE          NOT NULL,
    num_huespedes     SMALLINT      NOT NULL CHECK (num_huespedes > 0),
    estado            VARCHAR(20)   NOT NULL DEFAULT 'CONFIRMADA'
                      CHECK (estado IN ('CONFIRMADA','CANCELADA','FINALIZADA')),
    valor_total       NUMERIC(12,2) NOT NULL CHECK (valor_total >= 0),
    penalizacion      NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (penalizacion >= 0),
    fecha_creacion    TIMESTAMP     NOT NULL DEFAULT now(),
    fecha_cancelacion TIMESTAMP,
    fecha_check_in    TIMESTAMP,
    fecha_check_out   TIMESTAMP,
    CONSTRAINT ck_reserva_fechas CHECK (fecha_salida > fecha_entrada),
    -- Red de seguridad: la BD rechaza dos reservas activas que se crucen en la misma habitación.
    -- Rango [entrada, salida): la fecha de salida de una puede ser la entrada de otra.
    -- Solo las reservas CONFIRMADA bloquean el rango (CANCELADA y FINALIZADA liberan la habitación).
    CONSTRAINT ex_reserva_sin_cruce EXCLUDE USING gist (
        habitacion_id WITH =,
        daterange(fecha_entrada, fecha_salida, '[)') WITH &&
    ) WHERE (estado = 'CONFIRMADA')
);

CREATE INDEX idx_reserva_habitacion ON reserva(habitacion_id);
CREATE INDEX idx_reserva_huesped    ON reserva(huesped_id);
CREATE INDEX idx_reserva_estado     ON reserva(estado);
```

**Datos de ejemplo (seed):** 20 habitaciones (p. ej. 14 sencillas `101–114` y 6 suites `201–206`, con tarifas en COP), ~5 huéspedes y 3–4 reservas, incluyendo una cancelada. Dejarlos al final de `init.sql`.

**Diseño (para la sección 5 del informe):**
- FK `reserva → habitacion` y `reserva → huesped` con `ON DELETE RESTRICT`: no se puede borrar algo que tiene historial.
- `UNIQUE` en `habitacion.numero` y `huesped.documento`.
- `CHECK` en estados, fechas, capacidades y montos.
- Dinero siempre `NUMERIC` (nunca `double`).

## 8. Reglas de negocio y funcionalidades extra

### 8.1 Valor total de la estadía (extra #1)
- `noches = fechaSalida − fechaEntrada` (mínimo 1). La noche de salida no se cobra.
- El valor lo calcula **`Habitacion.calcularValorEstadia(noches)`** (polimorfismo; reglas en la sección 6.1).
- `Habitacion.desglosarValor(noches)` devuelve un `Map<String, BigDecimal>` ordenado, p. ej. `Subtotal`, `Recargo servicios`, `Descuento estadía larga`, `Total`. Se muestra en el formulario de nueva reserva (cotización en vivo al cambiar fechas o habitación) y en el detalle de la reserva.
- `CotizacionService.calculate(roomId, nights)` devuelve `CotizacionResponse` con subtotal, recargo, descuento y total (BigDecimal, escala 2, HALF_UP).
- El `valorTotal` se **guarda** en la reserva al crearla (así cambios futuros de tarifa no alteran reservas ya hechas).
- Dinero: `BigDecimal`, escala 2, `RoundingMode.HALF_UP`.

### 8.2 Cancelación de una reserva (extra #2)
- Solo se cancela una reserva `CONFIRMADA`. Nunca se borra físicamente: se conserva el historial.
- La fecha de referencia "hoy" se obtiene del bean `Clock` (zona `America/Bogota`), never `LocalDate.now()`.
- Política (constantes con nombre):
  - Faltan **2 o más días** para la entrada (`diasHastaEntrada >= 2`) → penalización **0**.
  - Faltan **menos de 2 días** o es el **mismo día de entrada** (inclusive, `diasHastaEntrada < 2` y `hoy <= fechaEntrada`) → penalización de **1 noche** de la tarifa.
  - La estadía ya comenzó (`hoy > fechaEntrada`) → `CancelacionNoPermitidaException`.
- Al crear una reserva se permite `fechaEntrada >= hoy` (inclusive hoy).
- Es **también una transacción de 2 tablas**: actualizar `reserva` (`CANCELADA`, `penalizacion`, `fechaCancelacion`) y **recalcular el estado de la habitación**: si no queda otra reserva `CONFIRMADA` vigente o futura → `DISPONIBLE`; si queda → `RESERVADA`.
- Al cancelar, el rango queda libre automáticamente (la restricción `EXCLUDE` ignora las canceladas y finalizadas).

### 8.3 Disponibilidad
- Una habitación está libre para `[entrada, salida)` si **no existe** una reserva no cancelada con `entrada_existente < salida_nueva AND salida_existente > entrada_nueva`, y su estado no es `MANTENIMIENTO`.
- Pantalla **Disponibilidad**: filtros de fechas, tipo y personas → tabla de habitaciones libres con el **valor total estimado** de cada una (llamada polimórfica).

### 8.4 Significado de `EstadoHabitacion` (decisión a justificar en el informe)
- `DISPONIBLE`: sin reservas activas vigentes o futuras.
- `RESERVADA`: tiene al menos una reserva `CONFIRMADA` vigente o futura. **El estado no decide la disponibilidad por fechas**; eso lo decide la consulta de cruce (8.3). El estado es un resumen para el tablero.
- `OCUPADA`: un huésped está alojado en la habitación después del check-in. Se usa en el flujo de check-in/check-out (dentro del alcance). La habitación pasa a OCUPADA al hacer check-in y vuelve a DISPONIBLE al hacer check-out.
- `MANTENIMIENTO`: no se puede reservar; la reserva falla con `HabitacionNoDisponibleException`.

### 8.5 Eliminación de habitaciones y huéspedes
- Habitación con reservas (cualquier estado) → **no se elimina**: `HabitacionConReservasException` con mensaje claro, sugiriendo ponerla en `MANTENIMIENTO`. Sin reservas → se elimina.
- Los huéspedes no se eliminan en el alcance mínimo (tienen historial).

## 9. Transacción obligatoria: crear reserva

Método: `ReservaServiceImpl.crearReserva(ReservaRequest)` (declarado en la interfaz `ReservaService`, devuelve `ReservaResponse`), anotado con `@Transactional` (público, invocado desde el controlador; no llamarlo desde otro método del mismo bean, porque la anotación no se aplicaría).

Orden de operaciones dentro de la transacción:

1. `SELECT … FROM habitacion WHERE id = ? FOR UPDATE` — bloquea la fila de la habitación para serializar reservas simultáneas.
2. Validar reglas: habitación existe y no está en `MANTENIMIENTO`; huésped existe; capacidad; fechas.
3. Consultar cruce de fechas (8.3). Si hay cruce → lanzar `ReservaCruzadaException`.
4. Calcular `valorTotal` con el polimorfismo (8.1).
5. `INSERT INTO reserva …` (tabla 1).
6. *(tabla 2: habitacion) no se cambia el estado al crear; la habitación pasa a `OCUPADA` en el check-in y a `DISPONIBLE` en el check-out.
7. *(solo perfil demo)* si `simularFallo` está activo → lanzar `SimulacionFalloException` **después** de guardar, para demostrar que ambos pasos se deshacen (rollback).

El campo `simularFallo` viaja en `ReservaRequest` pero su efecto solo se activa si el perfil `demo` está activo y `hotel.demo.simular-fallo-habilitado=true`.

Rollback:
- Todas las excepciones propias extienden `RuntimeException` (vía `HotelException`) → Spring hace rollback automático.
- Si la BD rechaza el `INSERT` por la restricción `ex_reserva_sin_cruce` (SQLState `23P01`; llega como `DataIntegrityViolationException`), el servicio la traduce a `ReservaCruzadaException` (caso de dos recepcionistas a la vez).
- El repositorio **no** abre ni cierra transacciones ni conexiones: eso lo hace Spring.

**Demostración del rollback en la sustentación** (preparar y ensayar):
- Caso A: reservar fechas que se cruzan → mensaje claro, nada guardado.
- Caso B: con el perfil `demo` (`./mvnw spring-boot:run -Dspring-boot.run.profiles=demo`) aparece una casilla "Simular fallo después de guardar". Se marca, se guarda, y se muestra en la BD (`docker exec -it hotel-db psql -U hotel -d hotel`) que **no hay fila nueva en `reserva` y el estado de la habitación no cambió**.
- La casilla solo existe si `hotel.demo.simular-fallo-habilitado=true`.

## 10. Vista (Thymeleaf) y controladores web

### 10.1 Pantallas

| Pantalla | Contenido | Acciones (las atiende su controlador) |
|---|---|---|
| **Tablero** (inicio) | Conteo de habitaciones por estado y reservas que entran/salen hoy | Actualizar |
| **Habitaciones** | Tabla con **filtros** (tipo, estado) | Nueva, Editar, Eliminar (con confirmación) → `habitacion/form.html` |
| **Huéspedes** | Tabla con **búsqueda** por documento o nombre | Nuevo → `huesped/form.html` |
| **Reservas** | Tabla con **filtros** (estado, documento) | Nueva reserva (`reserva/form.html` con cotización en vivo), Ver detalle (`reserva/detalle.html` con desglose), Cancelar, Check-in, Check-out |
| **Disponibilidad** | Fechas, tipo y personas → tabla de habitaciones libres con valor total estimado | Buscar; botón "Reservar esta" que va a `reserva/form.html` precargado |

### 10.2 Layout común (`fragments/layout.html`)

- **head**: título, meta viewport, Bootstrap 5.3 por CDN, Bootstrap Icons, estilos base.
- **navbar**: marca "Hotel Familiar", enlaces a Tablero, Habitaciones, Huéspedes, Reservas, Disponibilidad.
- **alerts**: fragmento que renderiza `mensaje` (success), `error` (danger), y errores globales de validación.
- **scripts**: Bootstrap JS bundle por CDN.

### 10.3 Cómo trabajan vista y controlador

- La **plantilla Thymeleaf** expone inputs con `th:field="*{campo}"` (binding bidireccional), selects con `th:each`, y muestra errores con `th:errorclass="is-invalid"` + `th:errors="*{campo}"`. No llama a servicios y no conoce al controlador.
- El **controlador web** (`@Controller`) es un `@Component` con los servicios inyectados por constructor; recibe peticiones HTTP, valida con `@Valid` + `BindingResult`, y devuelve nombre de plantilla + atributos en `Model`.
- Patrón de cada acción POST con formulario (ejemplo "guardar habitación"):
  1. `@Valid @ModelAttribute("habitacion") HabitacionRequest request, BindingResult errores`
  2. `if (errores.hasErrors()) { cargarCombos(model); return "habitacion/form"; }` — **MISMA vista**, datos conservados, errores junto a campos.
  3. `try { servicio.create(request); } catch (HotelException ex) { errores.reject("global", ex.getMessage()); cargarCombos(model); return "habitacion/form"; } catch (DataIntegrityViolationException ex) { ... }`
  4. `redirect.addFlashAttribute("mensaje", "..."); return "redirect:/habitaciones";` — PRG solo en éxito.
- **Excepciones de negocio** (`HotelException` y subclases): el controlador las catch, hace `errores.reject(codigo, mensajeAmigable)` y devuelve la misma vista.
- **Excepciones inesperadas**: `GlobalExceptionHandler` (web) renderiza `error.html` genérico en español con estado HTTP correcto; loguea con `@Slf4j`. `GlobalExceptionHandler` (API, `annotations = RestController.class`) devuelve JSON.

### 10.4 Validaciones (con mensajes claros, en español)

Cada capa valida lo suyo:

| Capa | Qué valida | Cómo |
|---|---|---|
| **DTO de entrada** (formato y obligatoriedad) | Campos vacíos, longitudes, correo, números positivos, fechas | Lombok + Bean Validation: `@NotBlank`, `@Size`, `@Email`, `@Positive`, `@DecimalMin`, `@NotNull`, `@FutureOrPresent`, `@DateTimeFormat(iso = ISO.DATE)`... con `message` propio |
| **DTO de entrada** (varios campos a la vez) | Salida posterior a la entrada | Método con `@AssertTrue(message = "La fecha de salida debe ser posterior a la de entrada")` en el DTO |
| **Entidad** (invariantes del dominio) | Tarifa > 0, capacidad 1–6, noches ≥ 1, huéspedes ≤ capacidad | Código escrito a mano en constructores/setters; lanza excepción con mensaje claro |
| **Servicio** (reglas de negocio) | Habitación libre, no en mantenimiento, capacidad real, política de cancelación | Lanza excepciones propias (`HotelException` y subclases) |
| **Base de datos** | Última red de seguridad | `CHECK`, `UNIQUE`, `FK`, `EXCLUDE` |

Errores de validación en plantilla: `th:errorclass="is-invalid"` en el input + `<div class="invalid-feedback" th:errors="*{campo}">`. Errores globales (`@AssertTrue`, excepciones de servicio): bloque `<div th:if="${#fields.hasGlobalErrors()}" class="alert alert-danger">` con `th:each="err : ${#fields.globalErrors()}" th:text="${err.defaultMessage}"` **dentro del `<form th:object="...">`**.

### 10.5 Manejo de errores (la app nunca muestra traza, JSON crudo, SQL ni mensajes técnicos)

`controller/exception/GlobalExceptionHandler`:
- `@ControllerAdvice(basePackages = "co.edu.ut.hotel.controller.web")` para controladores web: renderiza `error.html` con mensaje amigable y código HTTP (404 para recurso no encontrado, 500 genérico).
- `@RestControllerAdvice(annotations = RestController.class)` para APIs REST: devuelve JSON con `ErrorResponseDTO` (`codigoDeError`, `descripcion`, `hora`, `path`, `detalles`).
- `MethodArgumentNotValidException` (API): JSON 400 con detalles por campo.
- `DataAccessException`: detecta constraint `ex_reserva_sin_cruce` → mensaje "La habitación ya tiene una reserva confirmada en esas fechas" (web) o JSON 409 (API).
- `Exception` inesperada: log `@Slf4j` + mensaje genérico "Ocurrió un error inesperado" (web: `error.html`; API: JSON 500).

`application.yml`:
```yaml
server:
  error:
    whitelabel:
      enabled: false
    include-stacktrace: never
    include-message: never
spring:
  mvc:
    format:
      date: iso
```

## 11. Mapa de requisitos mínimos del documento

| # | Requisito | Cómo se cumple |
|---|---|---|
| 1 | BD con ≥2 tablas relacionadas por FK + script SQL | 3 tablas, FKs, `db/init.sql` |
| 2 | CRUD completo de la entidad principal; agregar y consultar en la secundaria | **Habitación** (entidad principal): crear, listar, editar, eliminar. **Huésped**: agregar y consultar (editar opcional). **Reserva**: crear, consultar, cancelar, check-in, check-out |
| 3 | Consultas parametrizadas | JPQL en `@Query` con parámetros nombrados (`:param`); cero concatenación |
| 4 | MVC separado | `controller/web` (C), `service` (M), plantillas Thymeleaf (V); dentro de `service`: `impl`, `entity`, `enums`, `dto`, `mapper`, `repository`, `exception` |
| 5 | Plantilla común con encabezado y menú | `fragments/layout.html` (head, navbar, alerts, scripts) |
| 6 | Validaciones con mensajes claros | 10.4 |
| 7 | Búsqueda o filtro en un listado | Habitaciones, huéspedes, reservas y disponibilidad |
| 8 | Transacción real en ≥2 tablas con rollback demostrable | Sección 9 (`reserva` + `habitacion` en cancelación; `reserva` en creación con `FOR UPDATE`) |
| 9 | Manejo de errores | 10.5 |

> Decisión editable: se eligió **Habitación** como entidad principal porque su CRUD completo no choca con la regla de no borrar reservas con historial. Si el equipo prefiere que la principal sea Reserva, hay que definir qué significa "eliminar" (p. ej. solo canceladas) y justificarlo.

## 12. Pruebas

**Unitarias (sin BD):**
- `HabitacionSencilla` y `HabitacionSuite`: valor para 1, 4 y 5+ noches (descuento de la suite en el umbral exacto).
- `Reserva.calcularPenalizacion`: 3 días antes, 1 día antes, mismo día, estadía ya iniciada.
- Validaciones de las entidades (fechas, capacidad, tarifa).
- Mappers: `HabitacionMapper` crea la subclase correcta según el tipo; `toResponse` copia bien los campos y el desglose.
- Bean Validation de los DTOs de entrada (campos vacíos, correo inválido, salida antes de la entrada).
- `GlobalExceptionHandler`: cada tipo de excepción (de negocio, de BD, inesperada) produce respuesta correcta (web: vista HTML limpia; API: JSON sin traza).
- Controladores web (`@WebMvcTest` + `@MockitoBean` + Jsoup): validaciones por campo con mensaje visible (`is-invalid` + texto), valores conservados, selects recargados, vistas y atributos del modelo, escapado.

**Integración (Testcontainers con PostgreSQL 17, usando el mismo `init.sql`):**
- Crear reserva válida → fila en `reserva` y estado `RESERVADA`.
- Reserva con cruce → `ReservaCruzadaException`, **no cambia nada**.
- Rollback: forzar fallo tras el `INSERT` → sin fila nueva y estado intacto.
- Dos reservas simultáneas por la misma habitación/fechas → solo una gana (restricción EXCLUDE).
- Cancelar → estado `CANCELADA`, penalización correcta, habitación `DISPONIBLE` si no quedan otras reservas.
- Eliminar habitación con reservas → rechazado.
- `@ParameterizedTest` de cruces: nuevo DENTRO de existente, nuevo CONTIENE existente, borde izquierdo, borde derecho, bordes exactos permitidos, cancelada que libera.
- Test que viole directamente la restricción EXCLUDE (saltando chequeo del servicio) → mensaje amigable.
- `RutasSmokeTest`: recorre `RequestMappingHandlerMapping`, todas las rutas GET sin variables → status < 500 y HTML limpio; rutas con `{id}` con id existente e inexistente.

**Tabla de pruebas manuales (para el informe, sección 8):** ID, descripción, pasos, resultado esperado, resultado obtenido, captura. Mínimo 12 casos cubriendo CRUD, filtros, validaciones, cruce de fechas, rollback, cancelación y mensajes de error. Las capturas son de las páginas web.

## 13. Comandos

```bash
docker compose up -d                       # PostgreSQL
./mvnw spring-boot:run                     # arranca servidor web en http://localhost:8081
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo   # con "simular fallo"
./mvnw test                                # pruebas (requiere Docker para Testcontainers)
./mvnw javadoc:javadoc                     # generar Javadoc
docker exec -it hotel-db psql -U hotel -d hotel          # consola SQL
docker compose down -v                     # reiniciar la BD desde cero
```

Requisitos locales: JDK 21, Docker. Verificar con `java -version` y `docker compose version`.
Para ejecutar sin Maven: `./mvnw package` y luego `java -jar target/hotel-*.jar` (con la BD levantada).

## 14. Fuera de alcance (y por qué — va en la sección 3 del informe)

- **Login/roles:** no es requisito y agrega complejidad que no se puede explicar en la sustentación; se menciona como mejora.
- **Pagos y facturación:** el caso trata de evitar cruces y calcular el valor, no de contabilidad.
- **Reprogramación de reservas:** se resuelve cancelando y creando otra.
- **Multi-hotel, temporadas y tarifas dinámicas, correos de confirmación.**
- **Versión escritorio (Swing):** la vista es web (Thymeleaf) por decisión del equipo; una versión Swing reutilizaría `service` casi sin cambios y solo reemplazaría `controller/web` y plantillas.

## 15. Insumos para el informe (el agente NO redacta el informe)

El informe (6–10 páginas, sin anexos) lo escribe **cada estudiante con sus palabras** y lo sube él mismo a la plataforma. El agente puede preparar en `docs/`:

- `diagrama-clases.mmd` — **UML de clases** (Mermaid o PlantUML) mostrando herencia, interfaces `XService`/`XDao` y relaciones entre clases.
- `diagrama-er.mmd` — **entidad-relación** de las 3 tablas.
- `paquetes-mvc.md` — esquema de paquetes y responsabilidad de cada capa (datos, no prosa final).
- `matriz-casos-vistas.md` — matriz de casos de prueba derivados de requisitos (ver FASE 2 del plan).
- `plantilla-tabla-pruebas.md` — tabla de pruebas vacía con los casos de la sección 12.
- `guion-rollback.md` — pasos exactos para la demostración del rollback.
- `preguntas-sustentacion.md` — lista de preguntas probables (POO, transacción, petición HTTP → controlador → servicio → repositorio → BD → plantilla) **para estudiar**, no respuestas listas para pegar.

El informe debe incluir: portada, análisis del caso, requisitos (incluyendo lo que se dejó fuera), decisiones técnicas justificadas, diseño de BD, arquitectura MVC, transacciones con evidencia del rollback, pruebas con capturas, dificultades reales, **aporte personal** de cada quien, conclusiones y anexos (script SQL, ZIP o enlace). Cualquier fuente consultada se cita.

## 16. Convenciones de código

- Identificadores y mensajes en **español**, sin tildes ni eñes en nombres de clases/métodos/variables (`Habitacion`, `fechaEntrada`); los textos al usuario sí llevan tildes.
- Una clase por archivo; clases pequeñas; métodos cortos con un solo propósito.
- **Javadoc** en español en clases públicas del modelo, servicios y repositorios: qué hace, parámetros, excepciones.
- Comentarios solo donde expliquen el *por qué* (reglas de negocio, decisiones); el código debe leerse solo.
- Constantes con nombre para reglas de negocio (porcentajes, umbrales, días de política de cancelación).
- Inyección por constructor con `@RequiredArgsConstructor` y campos `private final` (sin `@Autowired` en campos).
- Lombok según la sección 6.2; las validaciones del dominio van siempre escritas a mano.
- Las plantillas Thymeleaf no contienen lógica de negocio, SQL ni llamadas a servicios; los controladores solo usan DTOs; las entidades nunca llegan a la vista; los DTOs nunca llegan al repositorio.
- `LocalDate`/`LocalDateTime` y un bean `Clock` inyectado para poder probar las fechas.
- Logs con `@Slf4j`; nunca `System.out.println` ni `e.printStackTrace()`.
- Commits pequeños y frecuentes en Git; una rama por integrante o por funcionalidad. **Todos** revisan y entienden lo que se integra.

## 17. Orden de trabajo sugerido (FASE 4 del plan — bucle de iteración)

1. **AGENTS.md** reescrito (esta versión).
2. **Matriz de casos** en `docs/matriz-casos-vistas.md`.
3. **Dependencia jsoup** en `pom.xml` + `PaginaAsserts` helper.
4. **Tests que FALLEN** (`*ViewTest`, `*FlujoIT`, `RutasSmokeTest`) para cada hallazgo A–L.
5. **Correcciones** (bucle: test falla → corrige → test pasa):
   - `GlobalExceptionHandler`: separar web (HTML) vs API (JSON).
   - Plantillas: `th:errorclass="is-invalid"` + `th:errors`, errores globales dentro del form, `@{}` en hrefs.
   - DTOs: `@DateTimeFormat(iso = ISO.DATE)` en campos `LocalDate`.
   - Controladores: `cargarCombos(model)`, catch específico + `errores.reject()`, misma vista en error.
   - `TableroControllerWeb`: inyectar `Clock`.
   - `application.yml`: `server.error.whitelabel.enabled=false`, `spring.mvc.format.date=iso`.
   - `docker-compose.yml`: quitar comentario de Swing.
6. **Verificación**: `./mvnw test` completo pasa.
7. **Capturas** (Playwright o manual) en `docs/capturas/`.
8. **`docs/plantilla-tabla-pruebas.md`** desde la matriz.

## 18. Definition of Done (Checklist final)

- [ ] `AGENTS.md` describe UNA arquitectura (Thymeleaf + JPA) y coincide con pom.xml/código.
- [ ] `docker-compose.yml` sin referencias a Swing.
- [ ] `GlobalExceptionHandler`: web → `error.html`, API → JSON; sin trazas al usuario.
- [ ] Todas las plantillas: `th:errorclass="is-invalid"` + `th:errors`, errores globales dentro del `<form th:object>`, `@{}` en hrefs, `@DateTimeFormat` en DTOs.
- [ ] Controladores: `cargarCombos(model)` reutilizable; catch específico → `errores.reject()` + misma vista; PRG solo en éxito.
- [ ] `TableroControllerWeb` usa `Clock` inyectado.
- [ ] `ReservaControllerWeb.nuevoForm` lista habitaciones no en MANTENIMIENTO.
- [ ] `disponibilidad/lista.html` usa `@{/reservas/nuevo(...)} con fechas.
- [ ] Tests: `*ViewTest` (Jsoup), `*FlujoIT` (Testcontainers), `RutasSmokeTest` pasan.
- [ ] `./mvnw test` completo (110+ unitarias + IT) pasa.
- [ ] Matriz `docs/matriz-casos-vistas.md` cubierta 100% por tests.
- [ ] `docs/plantilla-tabla-pruebas.md` generada.
- [ ] Capturas en `docs/capturas/` (validación, cruce, éxito, lista vacía, error genérico).
- [ ] Cada integrante puede explicar: arquitectura, flujo petición, validación, transacción, rollback, manejo errores, decisión Thymeleaf vs Swing, decisión JPA vs JdbcTemplate, `@ControllerAdvice` vs `@RestControllerAdvice`, `th:errorclass`, `Clock`, EXCLUDE constraint.

---

## Política de errores web (resumen operativo)

1. **Validación (@Valid + BindingResult)**: el controlador devuelve la **MISMA vista** con el `BindingResult`; **nunca redirect**. Recarga todo lo que la plantilla necesita con un método privado `cargarCombos(model)`.
2. **Errores de negocio** (`HotelException` y subclases, `DataIntegrityViolationException` del constraint `ex_reserva_sin_cruce`, `IllegalArgumentException` del servicio): el controlador los captura, usa `errores.reject(codigo, mensajeAmigable)` y devuelve la **misma vista** con los datos escritos. Los mensajes al usuario son los de nuestras excepciones de negocio o un texto fijo; **nunca** `e.getMessage()` de otras excepciones.
3. **Acciones POST sin formulario propio** (cancelar, check-in, check-out, eliminar): pueden usar `redirect:` + flash, siempre con mensaje amigable.
4. **`@ControllerAdvice(basePackages = "co.edu.ut.hotel.controller.web")`** para lo inesperado: renderiza `error.html` genérico en español con el estado HTTP correcto (404 amigable para recurso inexistente) y loguea con `@Slf4j`.
5. **`@RestControllerAdvice(annotations = RestController.class)`** para APIs REST: devuelve JSON con `ErrorResponseDTO` (sin traza).
6. **`application.yml`**: `server.error.whitelabel.enabled=false`, `server.error.include-stacktrace=never`, `server.error.include-message=never`, `spring.mvc.format.date=iso`.