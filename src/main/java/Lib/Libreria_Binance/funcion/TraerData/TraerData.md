# Documentación de la Clase `TraerData`

La clase `TraerData` forma parte de la librería `Libreria_Binance` y permite acceder a distintas informaciones de la cuenta y del mercado en la API de Binance, haciendo uso del componente `Connect` para gestionar las solicitudes HTTP firmadas o públicas.

---

## Paquete

```java
package Lib.Libreria_Binance.funcion.TraerData;
```

## Dependencias

```java
import Lib.Libreria_Binance.funcion.Connect.Connect;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
```

---

## Constructores

### `TraerData(String apiKey, String secretKey)`

Constructor para entorno de producción.

### `TraerData(String apiKey, String secretKey, boolean isTestnet)`

Permite indicar si se desea usar el entorno de pruebas (testnet).

### `TraerData(String apiKey, String secretKey, String customBaseUrl)`

Permite indicar una URL personalizada para el endpoint base de Binance.

---

## Métodos

### `String obtenerDatasCuenta()`

Obtiene la información de la cuenta actual.

* Endpoint: `/api/v3/account`
* Requiere firma.

### `String obtenerHistorialTrades(String symbol)`

Obtiene el historial de trades de un símbolo.

* Parámetro: `symbol` (ej. BTCUSDT)
* Endpoint: `/api/v3/myTrades`
* Requiere firma.

### `String obtenerOrdenesAbiertas(String symbol)`

Obtiene las órdenes abiertas para un símbolo específico.

* Parámetro: `symbol` (ej. ETHUSDT)
* Endpoint: `/api/v3/openOrders`
* Requiere firma.

### `String obtenerTodasOrdenesAbiertas()`

Obtiene todas las órdenes abiertas de la cuenta.

* Endpoint: `/api/v3/openOrders`
* Requiere firma.

### `String obtenerPrecioSimbolo(String symbol)`

Obtiene el precio actual de un símbolo del mercado.

* Parámetro: `symbol` (ej. BTCUSDT)
* Endpoint: `/api/v3/ticker/price`
* Requiere firma.

### `String obtenerBalance()`

Obtiene el balance de la cuenta Binance.

* Endpoint: `/api/v3/account`
* Requiere firma.

### `boolean isTestnet()`

Indica si la instancia está trabajando sobre el entorno de pruebas.

### `String obtenerInfoServidor()`

Obtiene la hora del servidor Binance (uso público/test).

* Endpoint: `/api/v3/time`
* No requiere firma.

---

## Consideraciones

* Se recomienda manejar las excepciones `IOException` e `InterruptedException`.
* Todos los métodos que requieren firma utilizan el componente `Connect` para generar la firma HMAC-SHA256.
* Se recomienda validar los valores de `symbol` antes de hacer la solicitud para evitar errores HTTP.
* El método `publicRequest` de `Connect` se usa para endpoints sin autenticación.

---

## Posibles Errores de Binance

* `-1021`: Timestamp fuera de sincronización.
* `-1100`: Carácteres no permitidos en los parámetros.
* `-2014`: Apikey no válida.
* `-2015`: Firma inválida.
* `429`: Límites de velocidad excedidos.

Es recomendable implementar manejo de errores y backoff para solicitudes.

---

## Uso Recomendado

```java
TraerData data = new TraerData(apiKey, secretKey, true);
System.out.println(data.obtenerDatasCuenta());
System.out.println(data.obtenerPrecioSimbolo("BTCUSDT"));
```

---

## Licencia y Contacto

* Esta librería es de uso privado/personal.
* Si tienes dudas o sugerencias, contacta al desarrollador.
