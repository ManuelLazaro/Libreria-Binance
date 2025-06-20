package Lib.Libreria_Binance.funcion.RealTrades;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class RealTimeTrades implements WebSocket.Listener {

    private static final String BINANCE_WS_URL = "wss://stream.binance.com:9443/ws/";
    private final String symbol;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public RealTimeTrades(String symbol) {
        this.symbol = symbol.toLowerCase();
        connect();
    }

    private void connect() {
        HttpClient client = HttpClient.newHttpClient();
        client.newWebSocketBuilder()
                .buildAsync(URI.create(BINANCE_WS_URL + symbol + "@aggTrade"), this);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("📡 Conexión WebSocket abierta para " + symbol.toUpperCase());
        System.out.println("=== DATOS DE COMPRAS EN TIEMPO REAL ===");
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try {
            JsonNode json = objectMapper.readTree(data.toString());
            boolean isSell = json.get("m").asBoolean(); // m=true = venta, false = compra

            // Solo mostrar COMPRAS (no ventas)
            if (!isSell) {
                BigDecimal price = new BigDecimal(json.get("p").asText()); // precio
                BigDecimal qty = new BigDecimal(json.get("q").asText());   // cantidad
                String currentTime = LocalTime.now().format(timeFormatter);

                // Formato exacto que solicitas: precio, cantidad, hora
                System.out.printf("%s%n%s%n%s%n",
                        formatPrice(price),
                        formatQuantity(qty),
                        currentTime
                );
            }

        } catch (Exception e) {
            System.err.println("❌ Error al parsear mensaje: " + e.getMessage());
        }
        webSocket.request(1);
        return null;
    }

    private String formatPrice(BigDecimal price) {
        // Formato con comas para miles: 103,692.65
        return String.format("%,.2f", price);
    }

    private String formatQuantity(BigDecimal qty) {
        // Formato con 5 decimales: 0.00300
        return String.format("%.5f", qty);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("❌ Error en WebSocket: " + error.getMessage());
        // Intentar reconectar después de 5 segundos
        try {
            Thread.sleep(5000);
            connect();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("🔌 WebSocket cerrado: " + reason);
        // Intentar reconectar después de un breve delay
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000); // Esperar 2 segundos antes de reconectar
                connect();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        return null;
    }
}