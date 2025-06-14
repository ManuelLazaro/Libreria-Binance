package Lib.Libreria_Binance.funciones;

import Lib.Libreria_Binance.model.BinanceResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class BinanceConnector {

    private final String apiKey;
    private final String secretKey;
    private final String baseUrl;
    private final HttpClient httpClient;
    private boolean debugMode = true; // Activar modo debug por defecto

    private static final String BINANCE_API_URL = "https://api.binance.com";
    private static final String BINANCE_TESTNET_URL = "https://testnet.binance.vision";

    public BinanceConnector(String apiKey, String secretKey) {
        this(apiKey, secretKey, false);
    }

    public BinanceConnector(String apiKey, String secretKey, boolean useTestnet) {
        validarCredenciales(apiKey, secretKey);
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.baseUrl = useTestnet ? BINANCE_TESTNET_URL : BINANCE_API_URL;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        if (debugMode) {
            System.out.println("=== CONFIGURACIÓN BINANCE CONNECTOR ===");
            System.out.println("API Key: " + (apiKey != null ? apiKey.substring(0, 8) + "..." : "null"));
            System.out.println("Secret Key: " + (secretKey != null ? "***configurada***" : "null"));
            System.out.println("Base URL: " + baseUrl);
            System.out.println("Testnet: " + useTestnet);
            System.out.println("======================================\n");
        }
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    private void validarCredenciales(String apiKey, String secretKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key no puede ser null o vacía");
        }
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Secret Key no puede ser null o vacía");
        }
    }

    public BinanceResponse testServerConnection() {
        if (debugMode) System.out.println(">>> Probando conexión al servidor...");

        try {
            String endpoint = baseUrl + "/api/v3/time";
            if (debugMode) System.out.println("URL: " + endpoint);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (debugMode) {
                System.out.println("Código de respuesta: " + response.statusCode());
                System.out.println("Respuesta del servidor: " + response.body());
            }

            return new BinanceResponse(
                    response.statusCode() == 200,
                    response.statusCode(),
                    response.body(),
                    response.statusCode() == 200 ? "Conexión exitosa con el servidor" : "Error de conexión"
            );
        } catch (IOException | InterruptedException e) {
            if (debugMode) System.out.println("Error de conexión: " + e.getMessage());
            return new BinanceResponse(false, 0, null, "Error al conectar con el servidor: " + e.getMessage());
        }
    }

    public BinanceResponse testAuthentication() {
        if (debugMode) System.out.println(">>> Probando autenticación...");

        try {
            long timestamp = System.currentTimeMillis();
            String queryString = "timestamp=" + timestamp;
            String signature = generateSignature(queryString);
            String url = baseUrl + "/api/v3/account?" + queryString + "&signature=" + signature;

            if (debugMode) {
                System.out.println("Timestamp: " + timestamp);
                System.out.println("Query string: " + queryString);
                System.out.println("Signature: " + signature);
                System.out.println("URL completa: " + url);
                System.out.println("API Key (primeros 8 chars): " + apiKey.substring(0, 8) + "...");
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("X-MBX-APIKEY", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (debugMode) {
                System.out.println("Código de respuesta: " + response.statusCode());
                System.out.println("Headers de respuesta: " + response.headers().map());
                System.out.println("Cuerpo de respuesta: " + response.body());
            }

            return new BinanceResponse(
                    response.statusCode() == 200,
                    response.statusCode(),
                    response.body(),
                    response.statusCode() == 200 ? "Autenticación exitosa" : "Error de autenticación"
            );
        } catch (IOException | InterruptedException e) {
            if (debugMode) System.out.println("Error de autenticación: " + e.getMessage());
            return new BinanceResponse(false, 0, null, "Error al autenticar: " + e.getMessage());
        }
    }

    // Obtiene información de la cuenta
    public BinanceResponse getAccountInfo() {
        if (debugMode) System.out.println(">>> Obteniendo información de la cuenta...");

        try {
            long timestamp = System.currentTimeMillis();
            String queryString = "timestamp=" + timestamp;
            String signature = generateSignature(queryString);
            String url = baseUrl + "/api/v3/account?" + queryString + "&signature=" + signature;

            if (debugMode) {
                System.out.println("URL de cuenta: " + url);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("X-MBX-APIKEY", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (debugMode) {
                System.out.println("Código de respuesta (cuenta): " + response.statusCode());
                System.out.println("Respuesta (cuenta): " + response.body());
            }

            return new BinanceResponse(
                    response.statusCode() == 200,
                    response.statusCode(),
                    response.body(),
                    response.statusCode() == 200 ? "Información obtenida correctamente" : "Error al obtener información"
            );

        } catch (IOException | InterruptedException e) {
            if (debugMode) System.out.println("Error al obtener info de cuenta: " + e.getMessage());
            return new BinanceResponse(false, 0, null, "Error de conexión: " + e.getMessage());
        }
    }

    // Método adicional para verificar el estado de la API Key
    public BinanceResponse checkApiKeyStatus() {
        if (debugMode) System.out.println(">>> Verificando estado de la API Key...");

        try {
            long timestamp = System.currentTimeMillis();
            String queryString = "timestamp=" + timestamp;
            String signature = generateSignature(queryString);
            String url = baseUrl + "/api/v3/apiTradingStatus?" + queryString + "&signature=" + signature;

            if (debugMode) {
                System.out.println("URL API Key Status: " + url);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("X-MBX-APIKEY", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (debugMode) {
                System.out.println("Estado API Key - Código: " + response.statusCode());
                System.out.println("Estado API Key - Respuesta: " + response.body());
            }

            return new BinanceResponse(
                    response.statusCode() == 200,
                    response.statusCode(),
                    response.body(),
                    response.statusCode() == 200 ? "API Key funcionando correctamente" : "Error con la API Key"
            );

        } catch (IOException | InterruptedException e) {
            if (debugMode) System.out.println("Error verificando API Key: " + e.getMessage());
            return new BinanceResponse(false, 0, null, "Error al verificar API Key: " + e.getMessage());
        }
    }

    private String generateSignature(String data) {
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKeySpec);
            byte[] hash = sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            if (debugMode) {
                System.out.println("Datos para firma: " + data);
                System.out.println("Firma generada: " + result.toString());
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar la firma HMAC SHA256: " + e.getMessage(), e);
        }
    }
}