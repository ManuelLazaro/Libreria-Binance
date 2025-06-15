# Documentación de la clase `Tabla`

La clase `Tabla` está diseñada para procesar y visualizar respuestas JSON de la API de Binance en un formato tabular legible para el usuario en consola. Utiliza la librería `Jackson` para parsear JSON y presenta diferentes tipos de información como balances, trades, órdenes abiertas, precios, entre otros.

---

## Paquete

```java
package Lib.Libreria_Binance.funcion.Tabla;
```

## Dependencias

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
```

## Atributos

```java
private final ObjectMapper objectMapper;
private static final String SEPARATOR = "+";
private static final String VERTICAL = "|";
private static final String HORIZONTAL = "-";
```

## Constructor

```java
public Tabla()
```

Inicializa el `ObjectMapper` para parseo de JSON.

---

## Métodos Públicos

### `void mostrarDatosCuenta(String jsonResponse)`

Muestra información general de la cuenta y los balances en formato de tabla.

### `void mostrarHistorialTrades(String jsonResponse)`

Presenta el historial de operaciones (trades) realizadas por el usuario.

### `void mostrarOrdenesAbiertas(String jsonResponse)`

Muestra órdenes abiertas actuales del usuario.

### `void mostrarPrecioSimbolo(String jsonResponse)`

Visualiza el precio actual de un símbolo específico.

### `void mostrarInfoServidor(String jsonResponse)`

Muestra la hora del servidor y el timestamp correspondiente.

### `void mostrarJsonGenerico(String jsonResponse, String titulo)`

Muestra un JSON arbitrario como tabla, incluyendo estructuras anidadas.

---

## Métodos Privados

### `void mostrarBalances(JsonNode balances)`

Imprime los balances disponibles y bloqueados de todos los activos.

### `void mostrarTablaSimple(String titulo, String[][] datos)`

Dibuja una tabla básica con pares clave-valor.

### `void mostrarEncabezado(String[] headers, int[] widths)`

Genera el encabezado de una tabla con separadores.

### `void mostrarFila(String[] datos, int[] widths)`

Dibuja una fila de datos formateados.

### `void mostrarSeparador(int[] widths)`

Dibuja una línea horizontal separadora.

### `String crearTitulo(String titulo)`

Genera un título decorativo con borde.

### `String crearSubtitulo(String subtitulo)`

Crea un subtítulo simple con guiones.

### `String formatearTimestamp(long timestamp)`

Convierte un timestamp en formato legible `dd/MM/yyyy HH:mm:ss`.

### `String formatearDecimal(String valor)`

Formatea cadenas decimales a `8` cifras decimales o deja el texto original si no es numérico.

### `void mostrarObjetoJson(JsonNode node, String prefijo)`

Visualiza cualquier objeto JSON recursivamente, ideal para depuración.

---

## Casos de Uso

* Consola para monitoreo de cuentas Binance
* Debugging de respuestas JSON
* Visualización amigable para usuarios técnicos o desarrolladores

## Ejemplo de Uso

```java
Tabla tabla = new Tabla();
String respuesta = traerData.obtenerBalance();
tabla.mostrarDatosCuenta(respuesta);
```

---

## Consideraciones

* Solo se muestran balances con valores positivos.
* Si un campo contiene un `timestamp`, se intenta convertirlo a formato legible.
* Los arrays en `mostrarJsonGenerico` se muestran con numeración y anidamiento.

---

## Autor

Clase desarrollada como parte de la librería interna `Libreria_Binance` para visualización tabular de datos crudos provenientes de la API REST de Binance.
