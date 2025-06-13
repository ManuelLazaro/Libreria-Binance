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
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v3/time"))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return new BinanceResponse(
                    response.statusCode() == 200,
                    response.statusCode(),
                    response.body(),
                    response.statusCode() == 200 ? "Conexión exitosa con el servidor" : "Error de conexión"
            );
        } catch (IOException | InterruptedException e) {
            return new BinanceResponse(false, 0, null, "Error al conectar con el servidor: " + e.getMessage());
        }
    }

    public BinanceResponse testAuthentication() {
        try {
            long timestamp = System.currentTimeMillis();
            String queryString = "timestamp=" + timestamp;
            String signature = generateSignature(queryString);
            String url = baseUrl + "/api/v3/account?" + queryString + "&signature=" + signature;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("X-MBX-APIKEY", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return new BinanceResponse(
                    response.statusCode() == 200,
                    response.statusCode(),
                    response.body(),
                    response.statusCode() == 200 ? "Autenticación exitosa" : "Error de autenticación"
            );
        } catch (IOException | InterruptedException e) {
            return new BinanceResponse(false, 0, null, "Error al autenticar: " + e.getMessage());
        }
    }

    //Obtiene información de la cuenta
    public BinanceResponse getAccountInfo() {
        try {
            long timestamp = System.currentTimeMillis();
            String queryString = "timestamp=" + timestamp;
            String signature = generateSignature(queryString);

            String url = baseUrl + "/api/v3/account?" + queryString + "&signature=" + signature;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("X-MBX-APIKEY", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return new BinanceResponse(
                    response.statusCode() == 200,
                    response.statusCode(),
                    response.body(),
                    response.statusCode() == 200 ? "Información obtenida correctamente" : "Error al obtener información"
            );

        } catch (IOException | InterruptedException e) {
            return new BinanceResponse(false, 0, null, "Error de conexión: " + e.getMessage());
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
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar la firma HMAC SHA256: " + e.getMessage(), e);
        }
    }
}
