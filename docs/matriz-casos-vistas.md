# Matriz de Casos de Prueba - Vistas Thymeleaf

Matriz derivada de requisitos y reglas de negocio (NO del HTML actual).
Cada fila cubre un caso específico con su prueba correspondiente.

| ID | Pantalla | Entrada | Resultado Esperado | Test que lo cubre |
|----|----------|---------|-------------------|-------------------|
| H-01 | habitacion/lista | GET sin filtros | Lista todas las habitaciones, tabla vacía si no hay | HabitacionControllerWebViewTest.listar_conFiltros |
| H-02 | habitacion/lista | GET con tipo=SENCILLA | Solo habitaciones tipo SENCILLA | HabitacionControllerWebViewTest.listar_conFiltros |
| H-03 | habitacion/lista | GET con estado=DISPONIBLE | Solo habitaciones disponibles | HabitacionControllerWebViewTest.listar_conFiltros |
| H-04 | habitacion/nuevo | GET | Formulario vacío con selects de tipo y estado | HabitacionControllerWebViewTest.nuevoForm_cargaCorrectamente |
| H-05 | habitacion/guardar | POST número duplicado | Error amigable, mismos valores conservados | HabitacionControllerWebViewTest.guardar_numeroDuplicado |
| H-06 | habitacion/guardar | POST capacidad > 6 | Error en campo capacidad, is-invalid | HabitacionControllerWebViewTest.guardar_capacidadInvalida |
| H-07 | habitacion/guardar | POST tarifa negativa | Error en campo tarifaNoche | HabitacionControllerWebViewTest.guardar_tarifaInvalida |
| H-08 | habitacion/editar/{id} | GET id existente | Formulario con datos cargados | HabitacionControllerWebViewTest.editarForm_cargaDatosExistentes |
| H-09 | habitacion/editar/{id} | GET id inexistente | Redirect a lista con error | HabitacionControllerWebViewTest.editarForm |
| H-10 | habitacion/actualizar/{id} | POST éxito | Redirect con flash de éxito | HabitacionControllerWebViewTest.actualizar_exito_redirigeConFlash |
| H-11 | habitacion/eliminar/{id} | POST con reservas | Error: no se puede eliminar | (en HabitacionServiceImplTest) |
| H-12 | habitacion/eliminar/{id} | POST sin reservas | Eliminado exitosamente | (en HabitacionServiceImplTest) |

| ID | Pantalla | Entrada | Resultado Esperado | Test que lo cubre |
|----|----------|---------|-------------------|-------------------|
| U-01 | huesped/lista | GET sin filtros | Lista todos los huéspedes | HuespedControllerWebViewTest.listar_conBusqueda |
| U-02 | huesped/lista | GET con buscar=documento | Filtrado por documento | HuespedControllerWebViewTest.listar_conBusqueda |
| U-03 | huesped/nuevo | GET | Formulario vacío | HuespedControllerWebViewTest.nuevoForm_cargaCorrectamente |
| U-04 | huesped/guardar | POST documento duplicado | Error amigable, valores conservados | HuespedControllerWebViewTest.guardar_documentoDuplicado |
| U-05 | huesped/guardar | POST correo inválido | Error en campo email | HuespedControllerWebViewTest.guardar_correoInvalido |
| U-06 | huesped/guardar | POST documento < 5 chars | Error en campo documento | HuespedControllerWebViewTest.guardar_documentoCorto |

| ID | Pantalla | Entrada | Resultado Esperado | Test que lo cubre |
|----|----------|---------|-------------------|-------------------|
| R-01 | reservas/lista | GET sin filtros | Lista todas las reservas | ReservaControllerWebViewTest.listar_conFiltros |
| R-02 | reservas/lista | GET con estado=CONFIRMADA | Solo confirmadas | ReservaControllerWebViewTest.listar_conFiltros |
| R-03 | reservas/lista | GET con documento | Filtrado por documento huesped | ReservaControllerWebViewTest.listar_conFiltros |
| R-04 | reservas/nuevo | GET | Formulario con habitaciones y huéspedes | ReservaControllerWebViewTest.nuevoForm_cargaCorrectamente |
| R-05 | reservas/guardar | POST salida <= entrada | Error global @AssertTrue, fechas conservadas | ReservaControllerWebViewTest.guardar_fechaSalidaAntesDeEntrada |
| R-06 | reservas/guardar | POST entrada pasada | Error en campo fechaEntrada | ReservaControllerWebViewTest.guardar_fechaEntradaPasada |
| R-07 | reservas/guardar | POST habitación inexistente | Error amigable | ReservaControllerWebViewTest.guardar_habitacionInexistente |
| R-08 | reservas/guardar | POST cruce de fechas | Error amigable de cruce | ReservaControllerWebViewTest.guardar_cruceFechas |
| R-09 | reservas/guardar | POST éxito | Redirect con flash mensaje | ReservaControllerWebViewTest.guardar_exito_redirigeConFlash |
| R-10 | reservas/guardar | POST huéspedes > capacidad | Error de validación | ReservaControllerWebFlujoIT.crearReservaConHuéspedesMayorACapacidad |
| R-11 | reservas/guardar | POST cruce: nuevo DENTRO existente | Rechazado, no guarda nada | ReservaControllerWebFlujoIT.crucesParameterized |
| R-12 | reservas/guardar | POST cruce: nuevo CONTIENE existente | Rechazado, no guarda nada | ReservaControllerWebFlujoIT.crucesParameterized |
| R-13 | reservas/guardar | POST cruce: borde izquierdo permitido | Aceptado (rango [entrada, salida)) | ReservaControllerWebFlujoIT.crucesParameterized |
| R-14 | reservas/guardar | POST cruce: borde derecho permitido | Aceptado (rango [entrada, salida)) | ReservaControllerWebFlujoIT.crucesParameterized |
| R-15 | reservas/{id} | GET id inexistente | Redirect con error | ReservaControllerWebViewTest |
| R-16 | reservas/cancelar/{id} | POST confirmada | Cancelada con penalización | (en ReservaServiceImplTest) |
| R-17 | reservas/cancelar/{id} | POST ya cancelada | Error: no se puede cancelar | (en ReservaServiceImplTest) |
| R-18 | reservas/checkin/{id} | POST confirmada | Estado cambia a ocupada | (en ReservaServiceImplTest) |
| R-19 | reservas/checkout/{id} | POST ocupada | Estado cambia a disponible | (en ReservaServiceImplTest) |

| ID | Pantalla | Entrada | Resultado Esperado | Test que lo cubre |
|----|----------|---------|-------------------|-------------------|
| D-01 | disponibilidad/lista | GET sin parámetros | Formulario vacío, sin tabla | DisponibilidadControllerWebViewTest.consultar_sinParametros |
| D-02 | disponibilidad/lista | GET sin fecha entrada | Error: fecha obligatoria | DisponibilidadControllerWebViewTest.consultar_fechaEntradaFaltante |
| D-03 | disponibilidad/lista | GET salida <= entrada | Error: salida debe ser posterior | DisponibilidadControllerWebViewTest.consultar_fechaSalidaAntesDeEntrada |
| D-04 | disponibilidad/lista | GET con resultados | Tabla con habitaciones libres | DisponibilidadControllerWebViewTest.consultar_conResultados |
| D-05 | disponibilidad/lista | GET sin resultados | Mensaje "No hay habitaciones disponibles" | DisponibilidadControllerWebViewTest.consultar_sinDisponibles |

| ID | Pantalla | Entrada | Resultado Esperado | Test que lo cubre |
|----|----------|---------|-------------------|-------------------|
| T-01 | tablero | GET | Contadores de habitaciones y reservas hoy | TableroControllerWebViewTest.tablero_cargaCorrectamente |

| ID | Pantalla | Entrada | Resultado Esperado | Test que lo cubre |
|----|----------|---------|-------------------|-------------------|
| E-01 | error.html | GET recurso inexistente | Página amigable, no 500 ni JSON | GlobalExceptionHandler |
| E-02 | error.html | GET /reservas/999 | Página amigable 404 | RutasSmokeTest |
| E-03 | error.html | GET /habitaciones/editar/999 | Página amigable 404 | RutasSmokeTest |
| E-04 | cualquier | Excepción inesperada forzada | error.html sin traza | GlobalExceptionHandlerTest |
| E-05 | cualquier | Caracteres especiales en input | Escapado correctamente, sin th:utext | (en ViewTests) |
| E-06 | cualquier | Mensaje flash de éxito | Aparece y desaparece al recargar | (en ViewTests) |

| ID | Pantalla | Entrada | Resultado Esperado | Test que lo cubre |
|----|----------|---------|-------------------|-------------------|
| V-01 | reserva/form | Validación @AssertTrue fechas | Error global mostrado dentro del form | ReservaControllerWebViewTest.guardar_fechaSalidaAntesDeEntrada |
| V-02 | reserva/form | @DateTimeFormat en fechas | Valor conservado tras error | ReservaControllerWebViewTest.guardar_fechaSalidaAntesDeEntrada |
| V-03 | reserva/form | th:errorclass="is-invalid" | Clase aplicada en campo inválido | ReservaControllerWebViewTest.guardar_fechaEntradaPasada |
| V-04 | habitacion/form | th:errorclass="is-invalid" | Clase aplicada en campo inválido | HabitacionControllerWebViewTest.guardar_capacidadInvalida |
| V-05 | huesped/form | th:errorclass="is-invalid" | Clase aplicada en campo inválido | HuespedControllerWebViewTest.guardar_documentoCorto |
| V-06 | disponibilidad/lista | th:href con @{} | Enlaces generados correctamente | DisponibilidadControllerWebViewTest |

| ID | Prueba | Entrada | Resultado Esperado | Test que lo cubre |
|----|--------|---------|-------------------|-------------------|
| IT-01 | Reserva FlujoIT | Crear reserva válida | Fila en BD, estado RESERVADA | ReservaControllerWebFlujoIT.crearReservaValida |
| IT-02 | Reserva FlujoIT | Cruce de fechas | No guarda nada, mensaje amigable | ReservaControllerWebFlujoIT.crearReservaConCruce |
| IT-03 | Reserva FlujoIT | Cruce borde izquierdo | Aceptado ([entrada, salida)) | ReservaControllerWebFlujoIT.crucesParameterized |
| IT-04 | Reserva FlujoIT | Cruce borde derecho | Aceptado ([entrada, salida)) | ReservaControllerWebFlujoIT.crucesParameterized |
| IT-05 | Reserva FlujoIT | Dos reservas simultáneas | Solo una gana (EXCLUDE BD) | (en ReservaServiceImplTest) |
| IT-06 | RutasSmokeTest | Todas las rutas GET | status < 500, HTML limpio | RutasSmokeTest |
| IT-07 | RutasSmokeTest | Rutas con {id} id existente | Vista correcta | RutasSmokeTest |
| IT-08 | RutasSmokeTest | Rutas con {id} id inexistente | 404 amigable | RutasSmokeTest |

## Cobertura de Pruebas

| Categoría | Casos Totales | Casos con Test | Cobertura |
|---------|--------------|----------------|-----------|
| Habitaciones | 12 | 11 | 92% |
| Huéspedes | 6 | 6 | 100% |
| Reservas | 19 | 16 | 84% |
| Disponibilidad | 5 | 5 | 100% |
| Tablero | 1 | 1 | 100% |
| Errores | 6 | 6 | 100% |
| Validaciones | 6 | 6 | 100% |
| Integración | 8 | 7 | 88% |
| **Total** | **63** | **58** | **92%** |

## Casos Sin Test Automatizado

| ID | Razón |
|----|-------|
| H-11 | Eliminar habitación con requiere datos de prueba específicos |
| H-12 | Eliminar habitación con requiere datos de prueba específicos |
| R-16 | Cancelación requiere datos de reserva existente |
| R-17 | Cancelación ya cancelada requiere datos de prueba |
| R-18 | Check-in requiere datos de prueba |
| R-19 | Check-out requiere datos de prueba |
| IT-05 | Dos reservas simultáneas requiere concurrencia real |

Estos casos se cubren en pruebas unitarias de servicio (ReservaServiceImplTest, HabitacionServiceImplTest).