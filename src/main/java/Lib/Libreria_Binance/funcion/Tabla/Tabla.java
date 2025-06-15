package Lib.Libreria_Binance.funcion.Tabla;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class Tabla {
    private final ObjectMapper objectMapper;
    private static final String SEPARATOR = "+";
    private static final String VERTICAL = "|";
    private static final String HORIZONTAL = "-";

    public Tabla() {
        this.objectMapper = new ObjectMapper();
    }


    //Muestra los datos de la cuenta en formato tabla

    public void mostrarDatosCuenta(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            System.out.println("\n" + crearTitulo("INFORMACIÓN DE LA CUENTA"));

            // Información general
            String[][] datosGenerales = {
                    {"Maker Commission", root.path("makerCommission").asText()},
                    {"Taker Commission", root.path("takerCommission").asText()},
                    {"Buyer Commission", root.path("buyerCommission").asText()},
                    {"Seller Commission", root.path("sellerCommission").asText()},
                    {"Can Trade", root.path("canTrade").asText()},
                    {"Can Withdraw", root.path("canWithdraw").asText()},
                    {"Can Deposit", root.path("canDeposit").asText()},
                    {"Account Type", root.path("accountType").asText()},
                    {"Update Time", formatearTimestamp(root.path("updateTime").asLong())}
            };

            mostrarTablaSimple("Datos Generales", datosGenerales);

            // Balances
            JsonNode balances = root.path("balances");
            if (balances.isArray() && balances.size() > 0) {
                mostrarBalances(balances);
            }

        } catch (IOException e) {
            System.err.println("Error al procesar los datos de la cuenta: " + e.getMessage());
        }
    }


    //Muestra el historial de trades en formato tabla

    public void mostrarHistorialTrades(String jsonResponse) {
        try {
            JsonNode trades = objectMapper.readTree(jsonResponse);

            if (!trades.isArray() || trades.size() == 0) {
                System.out.println("No se encontraron trades.");
                return;
            }

            System.out.println("\n" + crearTitulo("HISTORIAL DE TRADES"));

            String[] headers = {"ID", "Symbol", "Side", "Quantity", "Price", "Commission", "Time"};
            int[] widths = {12, 12, 6, 15, 15, 12, 20};

            mostrarEncabezado(headers, widths);

            for (JsonNode trade : trades) {
                String[] fila = {
                        trade.path("id").asText(),
                        trade.path("symbol").asText(),
                        trade.path("isBuyer").asBoolean() ? "BUY" : "SELL",
                        formatearDecimal(trade.path("qty").asText()),
                        formatearDecimal(trade.path("price").asText()),
                        formatearDecimal(trade.path("commission").asText()),
                        formatearTimestamp(trade.path("time").asLong())
                };
                mostrarFila(fila, widths);
            }

            mostrarSeparador(widths);

        } catch (IOException e) {
            System.err.println("Error al procesar el historial de trades: " + e.getMessage());
        }
    }


    //Muestra las órdenes abiertas en formato tabla

    public void mostrarOrdenesAbiertas(String jsonResponse) {
        try {
            JsonNode ordenes = objectMapper.readTree(jsonResponse);

            if (!ordenes.isArray() || ordenes.size() == 0) {
                System.out.println("No hay órdenes abiertas.");
                return;
            }

            System.out.println("\n" + crearTitulo("ÓRDENES ABIERTAS"));

            String[] headers = {"Symbol", "Side", "Type", "Quantity", "Price", "Status", "Time"};
            int[] widths = {12, 6, 10, 15, 15, 12, 20};

            mostrarEncabezado(headers, widths);

            for (JsonNode orden : ordenes) {
                String[] fila = {
                        orden.path("symbol").asText(),
                        orden.path("side").asText(),
                        orden.path("type").asText(),
                        formatearDecimal(orden.path("origQty").asText()),
                        formatearDecimal(orden.path("price").asText()),
                        orden.path("status").asText(),
                        formatearTimestamp(orden.path("time").asLong())
                };
                mostrarFila(fila, widths);
            }

            mostrarSeparador(widths);

        } catch (IOException e) {
            System.err.println("Error al procesar las órdenes abiertas: " + e.getMessage());
        }
    }


    //Muestra información de precios

    public void mostrarPrecioSimbolo(String jsonResponse) {
        try {
            JsonNode precio = objectMapper.readTree(jsonResponse);

            System.out.println("\n" + crearTitulo("PRECIO DEL SÍMBOLO"));

            String[][] datos = {
                    {"Symbol", precio.path("symbol").asText()},
                    {"Price", formatearDecimal(precio.path("price").asText())}
            };

            mostrarTablaSimple("Información de Precio", datos);

        } catch (IOException e) {
            System.err.println("Error al procesar el precio del símbolo: " + e.getMessage());
        }
    }


   //Muestra los balances de la cuenta

    private void mostrarBalances(JsonNode balances) {
        System.out.println("\n" + crearSubtitulo("BALANCES"));

        String[] headers = {"Asset", "Free", "Locked", "Total"};
        int[] widths = {8, 18, 18, 18};

        mostrarEncabezado(headers, widths);

        for (JsonNode balance : balances) {
            double free = Double.parseDouble(balance.path("free").asText());
            double locked = Double.parseDouble(balance.path("locked").asText());
            double total = free + locked;

            // Solo mostrar balances que tengan algo
            if (total > 0) {
                String[] fila = {
                        balance.path("asset").asText(),
                        formatearDecimal(balance.path("free").asText()),
                        formatearDecimal(balance.path("locked").asText()),
                        String.format("%.8f", total)
                };
                mostrarFila(fila, widths);
            }
        }

        mostrarSeparador(widths);
    }


    //Muestra una tabla simple de clave-valor

    private void mostrarTablaSimple(String titulo, String[][] datos) {
        System.out.println("\n" + crearSubtitulo(titulo));

        int[] widths = {25, 30};
        String[] headers = {"Campo", "Valor"};

        mostrarEncabezado(headers, widths);

        for (String[] fila : datos) {
            mostrarFila(fila, widths);
        }

        mostrarSeparador(widths);
    }


    //Muestra el encabezado de la tabla

    private void mostrarEncabezado(String[] headers, int[] widths) {
        mostrarSeparador(widths);
        mostrarFila(headers, widths);
        mostrarSeparador(widths);
    }


    //Muestra una fila de datos

    private void mostrarFila(String[] datos, int[] widths) {
        System.out.print(VERTICAL);
        for (int i = 0; i < datos.length; i++) {
            String dato = datos[i];
            if (dato.length() > widths[i] - 2) {
                dato = dato.substring(0, widths[i] - 5) + "...";
            }
            System.out.printf(" %-" + (widths[i] - 2) + "s " + VERTICAL, dato);
        }
        System.out.println();
    }


    //Muestra el separador horizontal

    private void mostrarSeparador(int[] widths) {
        System.out.print(SEPARATOR);
        for (int width : widths) {
            for (int i = 0; i < width; i++) {
                System.out.print(HORIZONTAL);
            }
            System.out.print(SEPARATOR);
        }
        System.out.println();
    }


    //Crea un título principal

    private String crearTitulo(String titulo) {
        StringBuilder sb = new StringBuilder();
        int longitud = titulo.length() + 4;

        // Línea superior
        for (int i = 0; i < longitud; i++) sb.append("=");
        sb.append("\n");

        // Título
        sb.append("  ").append(titulo).append("  \n");

        // Línea inferior
        for (int i = 0; i < longitud; i++) sb.append("=");

        return sb.toString();
    }


    //Crea un subtítulo

    private String crearSubtitulo(String subtitulo) {
        return "--- " + subtitulo + " ---";
    }


    //Formatea un timestamp a fecha legible

    private String formatearTimestamp(long timestamp) {
        if (timestamp == 0) return "N/A";
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(date);
    }


    //Formatea números decimales

    private String formatearDecimal(String valor) {
        try {
            double num = Double.parseDouble(valor);
            if (num == 0) return "0.00000000";
            return String.format("%.8f", num);
        } catch (NumberFormatException e) {
            return valor;
        }
    }


    //Muestra información del servidor

    public void mostrarInfoServidor(String jsonResponse) {
        try {
            JsonNode info = objectMapper.readTree(jsonResponse);

            System.out.println("\n" + crearTitulo("INFORMACIÓN DEL SERVIDOR"));

            String[][] datos = {
                    {"Server Time", formatearTimestamp(info.path("serverTime").asLong())},
                    {"Timestamp", info.path("serverTime").asText()}
            };

            mostrarTablaSimple("Estado del Servidor", datos);

        } catch (IOException e) {
            System.err.println("Error al procesar la información del servidor: " + e.getMessage());
        }
    }


    //Muestra cualquier JSON en formato tabla genérica

    public void mostrarJsonGenerico(String jsonResponse, String titulo) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            System.out.println("\n" + crearTitulo(titulo.toUpperCase()));

            if (root.isObject()) {
                mostrarObjetoJson(root, "");
            } else if (root.isArray()) {
                System.out.println("Array con " + root.size() + " elementos:");
                for (int i = 0; i < root.size(); i++) {
                    System.out.println("\n--- Elemento " + (i + 1) + " ---");
                    mostrarObjetoJson(root.get(i), "");
                }
            } else {
                System.out.println("Valor: " + root.asText());
            }

        } catch (IOException e) {
            System.err.println("Error al procesar JSON: " + e.getMessage());
            System.out.println("Respuesta cruda: " + jsonResponse);
        }
    }


    //Muestra un objeto JSON de forma recursiva

    private void mostrarObjetoJson(JsonNode node, String prefijo) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();

            if (value.isObject()) {
                System.out.println(prefijo + key + ":");
                mostrarObjetoJson(value, prefijo + "  ");
            } else if (value.isArray()) {
                System.out.println(prefijo + key + ": [Array con " + value.size() + " elementos]");
            } else {
                String valorFormateado = value.asText();
                // Intentar formatear timestamps
                if (key.toLowerCase().contains("time") && valorFormateado.matches("\\d{13}")) {
                    valorFormateado += " (" + formatearTimestamp(value.asLong()) + ")";
                }
                System.out.println(prefijo + key + ": " + valorFormateado);
            }
        }
    }
}