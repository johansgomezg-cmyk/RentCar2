# RentCar — Parcial 2 de Programación II

Proyecto académico de gestión de alquiler de vehículos. Implementado en **Java 21**, **Gradle** y **JavaFX/FXML**, con siete patrones de diseño y una API REST **simulada** para pagos.

## 1. Integrantes

- Luis Eduardo Vélez Posada
- Johan Stiven Gómez

## 2. Problema y solución

RentCar permite registrar y consultar vehículos y clientes de ejemplo, construir solicitudes de alquiler, validar fechas y disponibilidad, cotizar de acuerdo con la modalidad, añadir servicios individuales o paquetes y procesar pagos de prueba. Una reservación solo se confirma si el flujo de pago devuelve **APPROVED** y la disponibilidad continúa siendo válida.

La pantalla **Reservations** permite seleccionar cliente, vehículo, fechas, modalidad, extras y token académico; consultar la cotización; ejecutar el checkout y observar su resultado e historial en memoria.

## 3. Requisitos implementados

| Requisito | Implementación principal | Comportamiento |
|---|---|---|
| RF-001 · Builder | `RentalRequestBuilder`, `RentalRequest` | Construcción y validación de solicitudes inmutables. |
| RF-002 · Facade | `RentalFacade`, `ReservationService`, `VehicleAvailabilityService` | Coordina cotización, disponibilidad, reserva pendiente, pago y confirmación. |
| RF-003 · Abstract Factory | `Rentalmode`, `Economy`, `Executive`, `Premium` | Crea políticas de tarifa, cobertura y asistencia por modalidad. |
| RF-004 · Adapter | `PaymentGateway`, `PaymentApiAdapter`, `PaymentOrder`, `PaymentResult` | Adapta el cliente proporcionado por el profesor sin duplicar el transporte HTTP. |
| RF-005 · Decorator | `RentalCharge`, `BaseRentalCharge`, `ServiceChargeDecorator` | Suma dinámicamente servicios y paquetes al costo base. |
| RF-006 · Proxy | `PaymentVerificationProxy`, `CustomerAuthorizationService` | Consulta autorización **antes** de enviar un pago al Adapter. |
| RF-007 · Composite | `RentalServiceItem`, `IndividualService`, `ServicePackage` | Representa servicios individuales y paquetes anidados con el mismo contrato. |

## 4. Arquitectura

- `model/`: entidades del dominio, solicitudes, resultados de cotización, tarifas y servicios.
- `service/`: disponibilidad, cotización, catálogo de extras, reserva y coordinación de Facade.
- `payment/`: contrato de pago, Adapter, Proxy y verificación de clientes.
- `repository/`: almacenamiento en memoria de vehículos.
- `controller/` y `resources/edu/rentcar/view/`: controladores y vistas JavaFX.
- `app/ApplicationServices`: comparte servicios entre vistas y conecta `PaymentVerificationProxy` con `PaymentApiAdapter`.

**Flujo:** JavaFX → `RentalRequestBuilder` → `RentalFacade.quote()` → `RentalFacade.checkout()` → `ReservationService` (Pending) → `PaymentVerificationProxy` → `PaymentApiAdapter` → API de prueba → interpretación de resultado → confirmación o estado de fallo.

### Estados de reservación

- `Pending`: reserva creada mientras se tramita el proceso.
- `Confirmed`: pago aprobado y disponibilidad revalidada.
- `PaymentDeclined`: pago rechazado o cliente no autorizado.
- `PaymentError`: falla técnica o confirmación fallida que requiere revisión.
- `Finalized`: estado definido en el modelo, sin un flujo completo de finalización implementado en esta entrega.

## 5. Justificación de patrones y pensamiento computacional

### RF-001 · Builder

**Problema:** una solicitud contiene campos y reglas de fechas que pueden validarse de forma inconsistente si se construye directamente. **Solución:** `RentalRequestBuilder` valida identificadores, fechas, modalidad y códigos de servicios antes de crear `RentalRequest`. **Ventaja:** construcción legible y centralizada. **Limitación:** la disponibilidad y el cobro no corresponden al Builder.

### RF-002 · Facade

**Problema:** la interfaz no debe coordinar por sí sola disponibilidad, políticas de tarifa, extras, pagos y estados de la reserva. **Solución:** `RentalFacade` expone `quote` y `checkout` y delega a los servicios. **Ventaja:** simplifica el punto de entrada del caso de uso. **Limitación:** no existe una transacción distribuida entre el servicio de pago y la reserva en memoria.

### RF-003 · Abstract Factory

**Problema:** Economy, Executive y Premium necesitan conjuntos consistentes de tarifa, cobertura y asistencia. **Solución:** cada implementación de `Rentalmode` crea las políticas correspondientes. **Ventaja:** familias de reglas coherentes y extensibles. **Limitación:** una nueva modalidad necesita una nueva implementación de fábrica/políticas.

### RF-004 · Adapter

**Problema:** el dominio no debe depender de los nombres ni de las respuestas HTTP del proveedor. **Solución:** `PaymentApiAdapter` transforma `PaymentOrder` a `PaymentApiRequest` y las respuestas a `PaymentResult`. **Ventaja:** desacopla pagos del proveedor. **Limitación:** depende del JAR y de que la API esté disponible. HTTP 200 no equivale por sí solo a aprobación: se verifica `APPROVED` frente a `NOT_APPROVED`.

### RF-005 · Decorator

**Problema:** el costo del alquiler se modifica según servicios opcionales combinables. **Solución:** `ServiceChargeDecorator` envuelve un `RentalCharge` y suma un `RentalServiceItem`. **Ventaja:** composición flexible sin crear una subclase por cada combinación. **Limitación:** es necesario evitar cargos duplicados; Facade valida los códigos y paquetes seleccionados.

### RF-006 · Proxy

**Problema:** un usuario no autorizado no puede acceder al proveedor de pagos. **Solución:** `PaymentVerificationProxy` implementa `PaymentGateway`, consulta `CustomerAuthorizationService` y solo delega si autoriza. **Ventaja:** la protección se aplica fuera de JavaFX y antes del Adapter. **Limitación pendiente de validar con el docente:** el ZIP revisado no incluía `CustomerVerificationService`; la implementación actual `InMemoryCustomerVerificationService` usa `Customer.isAuthorized()` y es una **sustitución provisional, no certificada como equivalente al servicio solicitado**.

### RF-007 · Composite

**Problema:** servicios sueltos y paquetes de servicios deben calcularse uniformemente, incluso con anidamiento. **Solución:** `IndividualService` y `ServicePackage` implementan `RentalServiceItem`, y los paquetes contienen otros elementos. **Ventaja:** permite paquetes anidados. **Limitación:** requieren validaciones de ciclos, vacíos y duplicados.

### Pensamiento computacional aplicado

1. **Descomposición:** separar solicitud, disponibilidad, cálculo, servicios, autorización, transporte, interpretación y reserva.
2. **Reconocimiento de patrones:** unificar los escenarios aprobado, rechazado, error técnico y no autorizado; identificar familias de tarifas y servicios combinables.
3. **Abstracción:** contratos `Rentalmode`, `RentalCharge`, `RentalServiceItem`, `PaymentGateway` y `CustomerAuthorizationService`.
4. **Algoritmo:** construir solicitud → validar fechas/vehículo → verificar disponibilidad → calcular tarifa y extras → crear Pending → autorizar → procesar pago → interpretar → revalidar disponibilidad → Confirmed o fallo.

## 6. Requisitos de ejecución

- JDK 21.
- Gradle Wrapper incluido (`gradlew` / `gradlew.bat`).
- Entorno gráfico para JavaFX.
- `libs/payment-client.jar`, `external/payment-api.jar` y `external/README-api-pagos.md`, suministrados en el proyecto.
- Puerto local `8080` libre durante las pruebas con la API simulada.

### Aplicación y API

En la primera terminal, desde la raíz del repositorio:

```bash
java -jar external/payment-api.jar
```

En la segunda, verificar:

```bash
curl -i http://localhost:8080/health
```

Debe devolver HTTP 200 con contenido `UP`. Otro servidor en ese puerto (por ejemplo, una interfaz web local) no sirve como sustituto de la API académica.

En una tercera terminal, ejecutar:

```bash
./gradlew run
```

En Windows usar `gradlew.bat run`. En JavaFX abrir **Reservations** y utilizar exclusivamente los tokens de prueba: `tok_approved`, `tok_declined` y `tok_api_error`. **No introducir datos bancarios reales**: no se procesan pagos reales.

## 7. Pruebas

Suite general:

```bash
./gradlew clean test
```

Casos de pago unitarios sin servidor (dobles de prueba):

```bash
./gradlew test --tests 'edu.rentcar.payment.PaymentApiAdapterTest'
./gradlew test --tests 'edu.rentcar.payment.PaymentVerificationProxyTest'
```

Prueba de integración con el JAR encendido (se habilita de manera explícita):

```bash
RENTCAR_INTEGRATION=true ./gradlew test --tests 'edu.rentcar.payment.PaymentApiIntegrationTest' --rerun-tasks
```

Los casos automatizados incluyen: `APPROVED`, `NOT_APPROVED`, HTTP 503, fallo de conexión, cliente autorizado, cliente rechazado (**cero llamadas al procesador**), error de verificación (**cero llamadas**), fechas superpuestas, servicios anidados y cotización/checkout. El equipo ha reportado ejecución exitosa de la suite; la evidencia final debe generarse nuevamente en `master` después de los últimos cambios.

### Demostración manual recomendada

1. Cliente autorizado + `tok_approved` → `Confirmed`.
2. Cliente autorizado + `tok_declined` → `PaymentDeclined`.
3. Cliente no autorizado + `tok_approved` → `PaymentDeclined` y ninguna llamada externa (demostrable por test).
4. Cliente autorizado + `tok_api_error` → `PaymentError`.
5. Solicitud sobre fechas ocupadas → no permite confirmar otra reserva coincidente.
6. Paquete `COMPLETE_PACK` + servicio `GPS` separado → impide cobro duplicado.

## 8. UML

- `docs/UML/RentCar_UML.mmd`: diagrama consolidado y coherente con las clases del código final.
- `docs/UML/RF-001-Builder.mmd`, `RF-002-Facade.mmd`, `RF-002-Vehicle-Availability.mmd`, `RF-004-Adapter.mmd`, `RF-005-Decorator.mmd`, `RF-006-Proxy.mmd`, `RF-007-Composite.mmd`.
- Abstract Factory: `docs/UML/RF-003-AbastractFactory.mmd` (nombre histórico del archivo, con errata; representa RF-003).

Los diagramas Mermaid se pueden previsualizar con una extensión compatible o exportar para el informe.

## 9. Limitaciones y decisiones

- Los datos y las reservas se guardan en memoria: desaparecen al cerrar la aplicación.
- El pago es **simulado** y depende del JAR académico en `localhost:8080`.
- `InMemoryCustomerVerificationService` es provisional hasta verificar con el docente la dependencia `CustomerVerificationService` mencionada en el enunciado; **no se afirma equivalencia**.
- No se implementan devolución, reversión ni conciliación automática: si el proveedor aprueba el pago pero la reserva no se confirma, se marca error para revisión manual.
- Aunque `ReservationService` sincroniza cambios individuales, checkout no constituye una operación atómica entre el proveedor externo y la reserva; se documenta la limitación ante concurrencia.
- La vista se debe comprobar manualmente después de cualquier modificación al FXML: las pruebas JUnit no validan por sí solas la carga y disposición de elementos JavaFX.
### Dependencia de verificación de clientes pendiente de validación

Durante la revisión de los archivos proporcionados para el parcial no se encontró la implementación de `CustomerVerificationService` mencionada en el enunciado.

Para permitir la integración y las pruebas del sistema, se implementó una solución provisional compuesta por:

- `CustomerAuthorizationService`: interfaz que define el contrato de autorización.
- `InMemoryCustomerVerificationService`: implementación que consulta los clientes almacenados en memoria y utiliza el atributo `authorized`.
- `PaymentVerificationProxy`: componente que verifica la autorización antes de delegar al proveedor de pagos.

Las pruebas unitarias verifican que un cliente no autorizado, o una falla del servicio de verificación, impiden la llamada al procesador de pagos.

**Limitación conocida:** esta implementación provisional no se considera automáticamente equivalente al servicio externo mencionado por el docente. Su integración o sustitución queda pendiente de confirmación y disponibilidad del componente original.

Esta decisión permite demostrar el funcionamiento del patrón Proxy, manteniendo explícita la limitación de la solución académica.

## 10. Evidencias y entrega

- Ramas y Pull Requests por funcionalidad/patrón en GitHub.
- Resultado actualizado de `./gradlew clean test` desde `master`.
- Demostración manual de aprobación y rechazo con API simulada.
- Diagramas Mermaid parciales y consolidado.
- Sustentación en video de ambos integrantes: explicar problema, patrón, clases, prueba y limitación.
