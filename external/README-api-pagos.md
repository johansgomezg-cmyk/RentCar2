# API REST de pagos — Guía de ejecución y pruebas

Esta guía explica cómo descargar, iniciar y probar la API REST de pagos utilizada en el parcial de PGII.

La API es un servicio **simulado para fines académicos**. No procesa pagos reales.

## 1. Requisitos

- **Java JDK 21** instalado.
- Una terminal: PowerShell en Windows, Git Bash en Windows o Terminal en macOS.
- El archivo `payment-api.jar`, incluido en el paquete proporcionado por el profesor.

No es necesario instalar Gradle, configurar una base de datos ni crear variables de entorno para ejecutar la API.

### Verificar Java

Ejecuta:

```text
java -version
```

Debes ver una versión de Java 21. Si el comando `java` no se reconoce, Java no está disponible en el `PATH`; configura la instalación de Java antes de continuar.

## 2. Descargar y preparar los archivos

1. Descarga el paquete ZIP de pagos proporcionado por el profesor.
2. Descomprime el ZIP.
3. Ubica el archivo `payment-api.jar`.
4. Abre una terminal en la carpeta donde se encuentra ese archivo.

La carpeta de trabajo debe contener, como mínimo:

```text
payment-api.jar
```

## 3. Ejecutar la API en Windows — PowerShell

1. Abre PowerShell en la carpeta donde está `payment-api.jar`.
2. Ejecuta:

```powershell
java -jar payment-api.jar
```

3. Deja esta ventana abierta mientras pruebas el servicio. La API escucha en:

```text
http://localhost:8080
```

### 3.1 Verificar que el servicio esté disponible

Abre una **segunda ventana de PowerShell** y ejecuta:

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/health" -Method Get
```

La respuesta esperada es HTTP `200` y el contenido `UP`.

### 3.2 Probar un pago aprobado

Copia y ejecuta esta línea completa en PowerShell:

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/payments" -Method Post -ContentType "application/json" -Body '{"reservationId":"RES-1001","customerId":"CUS-1001","amount":350000,"paymentToken":"tok_approved"}' | Select-Object StatusCode, Content
```

Resultado esperado:

```text
StatusCode Content
---------- -------
       200 APPROVED
```

### 3.3 Probar un pago rechazado

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/payments" -Method Post -ContentType "application/json" -Body '{"reservationId":"RES-1001","customerId":"CUS-1001","amount":350000,"paymentToken":"tok_declined"}' | Select-Object StatusCode, Content
```

Resultado esperado: HTTP `200` y `NOT_APPROVED`.

### 3.4 Probar un error técnico

```powershell
try { Invoke-WebRequest -Uri "http://localhost:8080/api/payments" -Method Post -ContentType "application/json" -Body '{"reservationId":"RES-1001","customerId":"CUS-1001","amount":350000,"paymentToken":"tok_api_error"}' } catch { Write-Host "HTTP status:" ([int]$_.Exception.Response.StatusCode); Write-Host $_.ErrorDetails.Message }
```

Resultado esperado: HTTP `503`.

> En PowerShell utiliza los comandos anteriores tal como aparecen. Para la prueba de pago se usa `Invoke-WebRequest`, que evita los problemas de comillas y formato JSON que pueden aparecer al usar `curl`.

## 4. Ejecutar y probar en Git Bash (Windows)

1. Abre Git Bash en la carpeta donde está `payment-api.jar`.
2. Ejecuta la API:

```bash
java -jar payment-api.jar
```

3. Deja la terminal abierta.
4. Abre una segunda ventana de Git Bash para las pruebas.

### 4.1 Verificar el servicio

```bash
curl -i http://localhost:8080/health
```

Resultado esperado: HTTP `200` y `UP`.

### 4.2 Pago aprobado

```bash
curl -i -X POST "http://localhost:8080/api/payments" -H "Content-Type: application/json" -d '{"reservationId":"RES-1001","customerId":"CUS-1001","amount":350000,"paymentToken":"tok_approved"}'
```

Resultado esperado: HTTP `200` y `APPROVED`.

### 4.3 Pago rechazado

```bash
curl -i -X POST "http://localhost:8080/api/payments" -H "Content-Type: application/json" -d '{"reservationId":"RES-1001","customerId":"CUS-1001","amount":350000,"paymentToken":"tok_declined"}'
```

Resultado esperado: HTTP `200` y `NOT_APPROVED`.

### 4.4 Error técnico

```bash
curl -i -X POST "http://localhost:8080/api/payments" -H "Content-Type: application/json" -d '{"reservationId":"RES-1001","customerId":"CUS-1001","amount":350000,"paymentToken":"tok_api_error"}'
```

Resultado esperado: HTTP `503`.

## 5. Ejecutar y probar en macOS

Los comandos son prácticamente los mismos que en Git Bash, porque ambos utilizan una sintaxis de shell similar. En macOS, abre **Terminal** y cambia a la carpeta donde está el JAR.

Por ejemplo, si está en Downloads:

```bash
cd ~/Downloads
```

Si el JAR está dentro de una carpeta descomprimida, entra en esa carpeta con `cd`.

### 5.1 Ejecutar la API

```bash
java -jar payment-api.jar
```

Deja esta terminal abierta y usa otra ventana o pestaña para las pruebas.

### 5.2 Verificar el servicio

```bash
curl -i http://localhost:8080/health
```

### 5.3 Pago aprobado

```bash
curl -i -X POST "http://localhost:8080/api/payments" -H "Content-Type: application/json" -d '{"reservationId":"RES-1001","customerId":"CUS-1001","amount":350000,"paymentToken":"tok_approved"}'
```

### 5.4 Pago rechazado

```bash
curl -i -X POST "http://localhost:8080/api/payments" -H "Content-Type: application/json" -d '{"reservationId":"RES-1001","customerId":"CUS-1001","amount":350000,"paymentToken":"tok_declined"}'
```

### 5.5 Error técnico

```bash
curl -i -X POST "http://localhost:8080/api/payments" -H "Content-Type: application/json" -d '{"reservationId":"RES-1001","customerId":"CUS-1001","amount":350000,"paymentToken":"tok_api_error"}'
```

Los resultados esperados son los mismos que en Git Bash.

## 6. Valores de `paymentToken`

| Token | Resultado esperado |
|---|---|
| `tok_approved` | HTTP `200` — `APPROVED` |
| `tok_declined` | HTTP `200` — `NOT_APPROVED` |
| `tok_api_error` | HTTP `503` — error técnico simulado |

## 7. Interpretar las respuestas

- **HTTP `200` + `APPROVED`:** el pago de prueba fue aprobado.
- **HTTP `200` + `NOT_APPROVED`:** el pago de prueba fue rechazado. El servicio respondió correctamente, pero el pago no fue aprobado.
- **HTTP `503`:** se simuló un error técnico del servicio.
- **Error de conexión:** revisa que el JAR siga ejecutándose en la primera terminal y que estés utilizando el puerto `8080`.

Un HTTP `200` no significa necesariamente que el pago haya sido aprobado: también debes revisar el texto de la respuesta.

## 8. Variables de entorno y configuración

No se requieren variables de entorno para ejecutar esta versión de la API. El puerto utilizado por defecto es `8080` y la URL base es `http://localhost:8080`.

## 9. Detener la API

Cuando termines las pruebas, vuelve a la terminal donde se está ejecutando el JAR y presiona:

```text
Ctrl+C
```

Esto detendrá el servicio.

## 10. Nota para el parcial

La API sirve para que los estudiantes prueben la integración de pagos. El profesor proporciona la API y la biblioteca cliente HTTP. Los estudiantes deben implementar los patrones de diseño exigidos en el enunciado; no deben desarrollar desde cero el transporte HTTP de este servicio.
