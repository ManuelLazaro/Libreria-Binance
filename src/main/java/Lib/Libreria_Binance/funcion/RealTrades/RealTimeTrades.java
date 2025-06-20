package Lib.Libreria_Binance.funcion.RealTrades;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RealTimeTrades implements WebSocket.Listener {

    private static final String BINANCE_WS_URL = "wss://stream.binance.com:9443/ws/";
    private final String symbol;

    private final AtomicInteger buyCount = new AtomicInteger(0);
    private final AtomicInteger sellCount = new AtomicInteger(0);
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
            int buys = buyCount.getAndSet(0);
            int sells = sellCount.getAndSet(0);
            System.out.println("📊 [" + Instant.now() + "] ➤ Compras: " + buys + " | Ventas: " + sells);
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
            boolean isSell = json.get("m").asBoolean();
            if (isSell) {
                sellCount.incrementAndGet();
            } else {
                buyCount.incrementAndGet();
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
