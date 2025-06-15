package Lib.Libreria_Binance.funcion.Connect;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;

public class Connect {
    private final String apiKey;
    private final String secretKey;
    private final HttpClient client;
    private final String baseUrl;

    // URLs para producción y testing
    private static final String PROD_URL = "https://api.binance.com";
    private static final String TEST_URL = "https://testnet.binance.vision";

    // Constructor para producción (comportamiento por defecto)
    public Connect(String apiKey, String secretKey) {
        this(apiKey, secretKey, false);
    }

    // Constructor que permite especificar si es testing
    public Connect(String apiKey, String secretKey, boolean isTestnet) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.baseUrl = isTestnet ? TEST_URL : PROD_URL;
        this.client = HttpClient.newHttpClient();
    }

    // Constructor que permite especificar URL personalizada
    public Connect(String apiKey, String secretKey, String customBaseUrl) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.baseUrl = customBaseUrl;
        this.client = HttpClient.newHttpClient();
    }

    public String signedRequest(String endpoint, Map<String, String> params) throws IOException, InterruptedException {
        long timestamp = Instant.now().toEpochMilli();
        params.put("timestamp", String.valueOf(timestamp));

        String queryString = buildQueryString(params);
        String signature = generateSignature(queryString);
        String finalUrl = baseUrl + endpoint + "?" + queryString + "&signature=" + signature;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(finalUrl))
                .header("X-MBX-APIKEY", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // Método para requests públicos (sin firma)
    public String publicRequest(String endpoint, Map<String, String> params) throws IOException, InterruptedException {
        String queryString = params.isEmpty() ? "" : "?" + buildQueryString(params);
        String finalUrl = baseUrl + endpoint + queryString;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(finalUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    private String generateSignature(String data) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKeySpec);
            byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error al generar la firma", e);
        }
    }

    // Método útil para saber si estamos en testnet
    public boolean isTestnet() {
        return TEST_URL.equals(baseUrl);
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}