package Lib.Libreria_Binance.funcion.RealTrades;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class RealTimeTrades implements WebSocket.Listener {

    private static final String BINANCE_WS_URL = "wss://stream.binance.com:9443/ws/";
    private final String symbol;

    private final AtomicReference<BigDecimal> buyTotal = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicReference<BigDecimal> sellTotal = new AtomicReference<>(BigDecimal.ZERO);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RealTimeTrades(String symbol) {
        this.symbol = symbol.toLowerCase();
        connect();
        startPrintStats();
    }

    private void connect() {
        HttpClient client = HttpClient.newHttpClient();
        client.newWebSocketBuilder()
                .buildAsync(URI.create(BINANCE_WS_URL + symbol + "@aggTrade"), this);
    }

    private void startPrintStats() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            BigDecimal buys = buyTotal.getAndSet(BigDecimal.ZERO);
            BigDecimal sells = sellTotal.getAndSet(BigDecimal.ZERO);
            System.out.printf("📊 [%s] ➤ Compras: $%,.2f | Ventas: $%,.2f%n",
                    Instant.now(), buys, sells);
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("📡 Conexión WebSocket abierta para " + symbol.toUpperCase());
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try {
            JsonNode json = objectMapper.readTree(data.toString());
            boolean isSell = json.get("m").asBoolean(); // m=true = venta, false = compra
            BigDecimal price = new BigDecimal(json.get("p").asText()); // precio
            BigDecimal qty = new BigDecimal(json.get("q").asText());   // cantidad

            BigDecimal total = price.multiply(qty); // valor en USD

            if (isSell) {
                sellTotal.updateAndGet(v -> v.add(total));
            } else {
                buyTotal.updateAndGet(v -> v.add(total));
            }

        } catch (Exception e) {
            System.err.println("❌ Error al parsear mensaje: " + e.getMessage());
        }
        webSocket.request(1);
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("❌ Error en WebSocket: " + error.getMessage());
    }
}
