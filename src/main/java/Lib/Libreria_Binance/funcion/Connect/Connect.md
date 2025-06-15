# Documentación: Clase `Connect`

Esta clase `Connect` proporciona la funcionalidad esencial para conectarse con la API REST de Binance, tanto en entorno de producción como en testnet. Permite realizar peticiones firmadas (autenticadas) y públicas (sin firma).

## Paquete

```java
package Lib.Libreria_Binance.funcion.Connect;
```

---

## Dependencias

```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
```

---

## Atributos

* `apiKey`: Clave pública de la API.
* `secretKey`: Clave secreta de la API.
* `client`: Cliente HTTP para enviar solicitudes.
* `baseUrl`: URL base de Binance (producción o testnet).

---

## Constructores

```java
public Connect(String apiKey, String secretKey)
```

> Usa por defecto el entorno de producción.

```java
public Connect(String apiKey, String secretKey, boolean isTestnet)
```

> Si `isTestnet` es `true`, se conecta al entorno de testnet.

```java
public Connect(String apiKey, String secretKey, String customBaseUrl)
```

> Permite definir una URL personalizada (por ejemplo, en casos de entornos internos o proxy).

---

## Métodos Públicos

### `signedRequest(String endpoint, Map<String, String> params)`

Realiza una petición firmada (privada) a Binance. Incluye firma HMAC-SHA256 y encabezado con la clave API.

### `publicRequest(String endpoint, Map<String, String> params)`

Realiza una petición pública (sin autenticación), usada para endpoints como `/api/v3/ticker/price`, etc.

### `boolean isTestnet()`

Devuelve `true` si la conexión actual está apuntando al entorno de testnet.

### `String getBaseUrl()`

Devuelve la URL base utilizada por la conexión.

---

## Métodos Privados

### `buildQueryString(Map<String, String> params)`

Convierte el mapa de parámetros en un string de consulta `key=value&key2=value2`.

### `generateSignature(String data)`

Genera una firma HMAC-SHA256 para los parámetros con la clave secreta.

---

## Ejemplo de Uso

```java
Connect connect = new Connect("API_KEY", "SECRET_KEY", true);
Map<String, String> params = new HashMap<>();
params.put("symbol", "BTCUSDT");
String response = connect.signedRequest("/api/v3/account", params);
System.out.println(response);
```

---

## Posibles Problemas con Binance API

* **Clock Drift**: Binance rechaza peticiones si la diferencia de tiempo entre cliente y servidor es demasiado grande.
* **LIMITS**: La API tiene límites de uso (weight limits). Un exceso de peticiones puede causar errores HTTP 429.
* **API Key Restrictions**: Asegúrate de que las claves estén habilitadas para testnet o mainnet según el entorno.
* **Invalid Signature**: Una mala firma (por mal timestamp o mal orden de parámetros) generará error 401 o 400.
* **recvWindow**: Si no se ajusta este valor, pueden generarse errores por pequeñas diferencias de tiempo.

---

## Recomendaciones

* Usa `recvWindow` en tus peticiones firmadas para tener tolerancia en tiempo.
* Valida los parámetros antes de firmarlos.
* Captura y maneja correctamente las excepciones `IOException`, `InterruptedException` y `RuntimeException`.

---

## Licencia

Este código es parte de una librería personalizada para acceder a la API de Binance.

> © Manuel Lázaro - Uso educativo y de prueba.
