# Notas para el docente — RentCar Starter

Este documento es de apoyo docente y no forma parte de la documentación que los estudiantes deben completar en su entrega. No distribuirlo con el paquete del estudiante si se quiere mantener estas notas internas.

## Contenido de la base

- Java 21, Gradle Wrapper y JavaFX/FXML.
- Vistas separadas: dashboard, customers, vehicles y reservations.
- `MainController` se limita a la navegación.
- El módulo de vehículos es un ejemplo funcional de MVC con controller, service, repository y model.
- Pruebas unitarias de ejemplo para `VehicleService`.
- `libs/payment-client.jar` es el cliente de pagos proporcionado.
- `external/payment-api.jar` y `external/README-api-pagos.md` contienen el API simulado y sus instrucciones.
- El flujo de reservas y los patrones evaluados permanecen sin implementar intencionalmente.

## Ejecutar desde IntelliJ IDEA

1. Abrir la carpeta raíz `RentCar-Starter` como proyecto Gradle.
2. Confirmar que el Project SDK y el Gradle JVM sean JDK 21.
3. Esperar a que termine la sincronización de Gradle.
4. Ejecutar `application > run` desde la ventana Gradle.
5. Para ejecutar pruebas, usar `verification > test` o la terminal integrada.

Si el botón verde de IntelliJ no ejecuta la aplicación, verificar primero que el proyecto fue importado como Gradle y que el Gradle JVM sea Java 21. La ruta más reproducible es usar el Gradle Wrapper:

- PowerShell: ` .\gradlew.bat run `
- Git Bash/macOS/Linux: ` ./gradlew run `

Para las pruebas:

- PowerShell: ` .\gradlew.bat test `
- Git Bash/macOS/Linux: ` ./gradlew test `

## API de pagos

Las instrucciones de ejecución, endpoints, respuestas y tokens de prueba se mantienen en `external/README-api-pagos.md`. El archivo `external/payment-api.jar` debe ejecutarse como proceso separado si se va a probar la integración real con el servicio simulado. El cliente que consumen los estudiantes está en `libs/payment-client.jar`.

No es necesario copiar la documentación técnica del API al README principal del estudiante; el enunciado debe especificar el contrato que necesitan conocer y el archivo de referencia del API se puede entregar por separado.

## ApplicationServices: decisión arquitectónica

`app/ApplicationServices.java` es una solución pequeña para compartir la misma instancia de `VehicleService` entre vistas. Técnicamente funciona como un localizador de servicios estático; no es un contenedor de inyección de dependencias.

Para el starter actual, se puede conservar para no añadir complejidad accidental a la base. Si el objetivo docente incluye evaluar inyección de dependencias, una mejora posterior sería crear las dependencias en `RentCarApplication` y suministrarlas a los controllers mediante una `controllerFactory` de `FXMLLoader` o mediante constructores. No se necesita incorporar Spring ni otro framework para demostrar inyección de dependencias.

## Alcance que deben completar los estudiantes

Seguir el enunciado y la rúbrica oficiales. La base no implementa el proceso completo de reserva, cálculo de disponibilidad, precios, servicios opcionales, integración mediante Adapter/Proxy ni la coordinación del flujo mediante Facade. No agregar esas soluciones al starter si deben ser evaluadas.


## Estructura del ejemplo MVC con JavaFX

- `view/main-view.fxml` + `MainController`: estructura principal de la aplicación y navegación únicamente.
- `view/dashboard-view.fxml` + `DashboardController`: información del panel principal.
- `view/customer-view.fxml` + `CustomerController`: tabla de clientes.
- `view/vehicle-view.fxml` + `VehicleController`: registro de vehículos y tabla.
- `view/reservation-view.fxml` + `ReservationController`: estructura inicial para desarrollar el parcial.
- `model/Vehicle.java`: datos del vehículo.
- `repository/VehicleRepository.java`: almacenamiento de vehículos en memoria.
- `service/VehicleService.java`: operaciones de validación y registro.
- `app/ApplicationServices.java`: comparte la instancia del servicio de vehículos entre las vistas de este proyecto base.

`MainController` carga una pantalla a la vez en el área central del contenido. El archivo FXML de cada pantalla declara su propio controller mediante `fx:controller`. Las vistas comparten el servicio de vehículos que almacena los datos en memoria, por lo que los vehículos registrados en la pantalla `Vehicles` siguen disponibles al navegar a otra pantalla y regresar, durante la misma ejecución de la aplicación.