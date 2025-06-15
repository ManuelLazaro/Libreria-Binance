package Lib.Libreria_Binance.funcion.TraerData;

import Lib.Libreria_Binance.funcion.Connect.Connect;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TraerData {

    private final Connect connect;

    // Constructor para producción
    public TraerData(String apiKey, String secretKey) {
        this.connect = new Connect(apiKey, secretKey);
    }

    // Constructor que permite especificar si es testnet
    public TraerData(String apiKey, String secretKey, boolean isTestnet) {
        this.connect = new Connect(apiKey, secretKey, isTestnet);
    }

    // Constructor que permite URL personalizada
    public TraerData(String apiKey, String secretKey, String customBaseUrl) {
        this.connect = new Connect(apiKey, secretKey, customBaseUrl);
    }

    // Obtiene información de la cuenta actual
    public String obtenerDatasCuenta() throws IOException, InterruptedException {
        Map<String, String> params = new HashMap<>();
        params.put("recvWindow", "5000");
        return connect.signedRequest("/api/v3/account", params);
    }

    // Obtiene el historial de trades para un símbolo específico
    public String obtenerHistorialTrades(String symbol) throws IOException, InterruptedException {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("El símbolo no puede ser nulo o vacío");
        }

        Map<String, String> params = new HashMap<>();
        params.put("symbol", symbol.toUpperCase());
        params.put("limit", "500");
        return connect.signedRequest("/api/v3/myTrades", params);
    }

    // Obtiene las órdenes abiertas para un símbolo específico
    public String obtenerOrdenesAbiertas(String symbol) throws IOException, InterruptedException {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("El símbolo no puede ser nulo o vacío");
        }

        Map<String, String> params = new HashMap<>();
        params.put("symbol", symbol.toUpperCase());
        return connect.signedRequest("/api/v3/openOrders", params);
    }

    // Obtiene todas las órdenes abiertas de la cuenta
    public String obtenerTodasOrdenesAbiertas() throws IOException, InterruptedException {
        Map<String, String> params = new HashMap<>();
        return connect.signedRequest("/api/v3/openOrders", params);
    }

    // Obtiene información de precios de un símbolo específico
    public String obtenerPrecioSimbolo(String symbol) throws IOException, InterruptedException {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("El símbolo no puede ser nulo o vacío");
        }

        Map<String, String> params = new HashMap<>();
        params.put("symbol", symbol.toUpperCase());
        return connect.signedRequest("/api/v3/ticker/price", params);
    }

    // Obtiene el balance de la cuenta
    public String obtenerBalance() throws IOException, InterruptedException {
        Map<String, String> params = new HashMap<>();
        params.put("recvWindow", "5000");
        return connect.signedRequest("/api/v3/account", params);
    }

    // Método útil para saber en qué entorno estamos
    public boolean isTestnet() {
        return connect.isTestnet();
    }

    // Obtiene información pública del servidor (útil para testing)
    public String obtenerInfoServidor() throws IOException, InterruptedException {
        Map<String, String> params = new HashMap<>();
        return connect.publicRequest("/api/v3/time", params);
    }
}