package Lib.Libreria_Binance.funcion.Connect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConnectionInfo {
    private final Connect connection;
    private final ObjectMapper objectMapper;

    public ConnectionInfo(Connect connection) {
        this.connection = connection;
        this.objectMapper = new ObjectMapper();
    }

    public void obtenerInformacionConexion() {
        try {
            verificarConectividad();
            obtenerInfoCuenta();
            obtenerInfoExchange();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void verificarConectividad() throws IOException, InterruptedException {
        String pingResponse = connection.publicRequest("/api/v3/ping", new HashMap<>());
        System.out.println("Conexión: " + (pingResponse.equals("{}") ? "OK" : "Error"));

        String timeResponse = connection.publicRequest("/api/v3/time", new HashMap<>());
        JsonNode timeNode = objectMapper.readTree(timeResponse);
        System.out.println("Tiempo servidor: " + new java.util.Date(timeNode.get("serverTime").asLong()));
    }

    private void obtenerInfoCuenta() throws IOException, InterruptedException {
        String accountInfo = connection.signedRequest("/api/v3/account", new HashMap<>());
        JsonNode accountNode = objectMapper.readTree(accountInfo);

        System.out.println("Modo: " + (connection.isTestnet() ? "TESTNET" : "PRODUCCIÓN"));
        System.out.println("Tipo cuenta: " + accountNode.get("accountType").asText());
        System.out.println("Trading habilitado: " + accountNode.get("canTrade").asBoolean());
        System.out.println("Retiros habilitados: " + accountNode.get("canWithdraw").asBoolean());
        System.out.println("Depósitos habilitados: " + accountNode.get("canDeposit").asBoolean());
        System.out.println("Tiempo actualización: " + new java.util.Date(accountNode.get("updateTime").asLong()));

        // Contar cantidad de activos con balance
        JsonNode balances = accountNode.get("balances");
        int activosConBalance = 0;
        for (JsonNode balance : balances) {
            double free = balance.get("free").asDouble();
            double locked = balance.get("locked").asDouble();
            if (free > 0 || locked > 0) {
                activosConBalance++;
            }
        }
        System.out.println("Activos con balance: " + activosConBalance);
        System.out.println("Total activos disponibles: " + balances.size());
    }

    private void obtenerInfoExchange() throws IOException, InterruptedException {
        String exchangeInfo = connection.publicRequest("/api/v3/exchangeInfo", new HashMap<>());
        JsonNode exchangeNode = objectMapper.readTree(exchangeInfo);

        System.out.println("Zona horaria: " + exchangeNode.get("timezone").asText());

        JsonNode symbols = exchangeNode.get("symbols");
        int paresActivos = 0;
        for (JsonNode symbol : symbols) {
            if ("TRADING".equals(symbol.get("status").asText())) {
                paresActivos++;
            }
        }
        System.out.println("Pares de trading activos: " + paresActivos);
        System.out.println("Total pares disponibles: " + symbols.size());

        // Rate limits info
        JsonNode rateLimits = exchangeNode.get("rateLimits");
        System.out.println("Límites de velocidad configurados: " + rateLimits.size());
    }

    public void obtenerEstadisticasCuenta() {
        try {
            String accountInfo = connection.signedRequest("/api/v3/account", new HashMap<>());
            JsonNode accountNode = objectMapper.readTree(accountInfo);

            System.out.println("=== ESTADÍSTICAS DE CUENTA ===");
            System.out.println("Comisión maker: " + accountNode.get("makerCommission").asInt() + " (0.1% = 10)");
            System.out.println("Comisión taker: " + accountNode.get("takerCommission").asInt() + " (0.1% = 10)");
            System.out.println("Comisión compra: " + accountNode.get("buyerCommission").asInt());
            System.out.println("Comisión venta: " + accountNode.get("sellerCommission").asInt());

            JsonNode balances = accountNode.get("balances");
            System.out.println("Monedas soportadas por la cuenta: " + balances.size());

            if (connection.isTestnet()) {
                System.out.println("Fondos de prueba: ILIMITADOS (solicitar via faucet)");
                System.out.println("Valor real: $0 USD");
            }

        } catch (Exception e) {
            System.err.println("Error estadísticas: " + e.getMessage());
        }
    }
}