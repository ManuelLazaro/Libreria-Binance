# 📊 Documentación de Uso - Librería Binance (Versión Inicial)

## ✨ Propósito

Esta librería está diseñada para facilitar la integración con la **API oficial de Binance** desde aplicaciones Java. El objetivo inicial es ofrecer una implementación funcional para realizar **consultas autenticadas** mediante peticiones firmadas usando la clase `Connect`, la cual ya se encuentra implementada.

Este documento describe:

* El uso actual de la librería.
* Casos de uso.
* Posibles errores comunes al trabajar con la API de Binance.
* Futuras extensiones de la librería.

---

## 📅 Estado Actual

Por el momento, la librería cuenta con:

### ✅ Clase Implementada: `Connect`

* Permite hacer solicitudes HTTP GET **firmadas**.
* Se usa para conectarse a endpoints privados de Binance que requieren autenticación (`apiKey` y `secretKey`).
* Utiliza `HttpClient` de Java 11+.

---

## 🚀 Primer caso de uso: Obtener datos privados

La clase `Connect` puede ser usada para recuperar información sensible/autenticada, como:

* Estado de la cuenta.
* Balances.
* Historial de trades.
* Ordenes abiertas o cerradas.

### Ejemplo:

```java
Connect connect = new Connect("TU_API_KEY", "TU_SECRET_KEY");

Map<String, String> params = new HashMap<>();
params.put("recvWindow", "5000");

String json = connect.signedRequest("/api/v3/account", params);
System.out.println(json);
```

---

## ⚠️ Posibles Problemas con Binance

### 1. ❌ Timestamp desincronizado

* Error: `-1021: Timestamp for this request is outside of the recvWindow`
* Solución: Asegúrate de que tu sistema tenga sincronización horaria (NTP).

### 2. ⛔ Límites de peticiones

* Binance impone **rate limits**.
* Error: `-1003 TOO_MANY_REQUESTS`
* Solución: Implementar lógica de reintento y esperar antes de reenviar.

### 3. 🔑 API Key o Secret incorrectos

* Error: `-2015 Invalid API-key, IP, or permissions`
* Solución: Verifica las claves y permisos desde tu cuenta de Binance.

### 4. 💸 Requiere firma pero no la tiene

* Error: `-1022 Signature for this request is not valid`
* Solución: Asegúrate de que todos los parámetros estén firmados correctamente con `HMAC SHA256`.

---

## 💡 Recomendaciones

* Usa siempre `recvWindow` para controlar el margen de tiempo permitido por Binance.
* Maneja respuestas de error con mensajes claros y captura de excepción.
* Mantén seguras tus `API Key` y `Secret Key`.
* No expongas tus credenciales en código fuente o repositorios.

---

## 🚜 Futuras Extensiones (Planeado)

* [ ] Soporte para métodos `POST`, `PUT`, `DELETE` firmados.
* [ ] Clase para solicitudes sin firma (públicas).
* [ ] Parsers automáticos para convertir JSON en objetos Java (`Gson` o `Jackson`).
* [ ] Integración con WebSockets para precios en tiempo real.
* [ ] Wrapper de alto nivel para operar directamente (ordenar, cerrar, cancelar).

---

## 📦 Dependencias

* Java 11+ (por `HttpClient`).
* Ninguna dependencia externa por el momento.

---

## 🔒 Seguridad

* Las claves nunca deben almacenarse en texto plano.
* Recomendado: usar `.env` o servicios seguros de almacenamiento de credenciales.

---

## 🚀 Próximos pasos

Puedes comenzar a integrar la clase `Connect` para tus consultas firmadas de Binance. Pronto estará disponible el soporte para operaciones más complejas.

---

Para dudas o soporte, documenta bien tus llamadas y errores obtenidos. Esta librería se encuentra en fase inicial, por lo que tu retroalimentación es vital para su mejora.
