package Lib.Libreria_Binance.funciones;

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
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BinanceConnector {

    private static final String BINANCE_API_URL = "https://api.binance.com";
    private static final String BINANCE_US_API_URL = "https://api.binance.us";
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final String apiKey;
    private final String secretKey;
    private final String baseUrl;
    private final HttpClient httpClient;

    public BinanceConnector(String apiKey, String secretKey) {
        this(apiKey, secretKey, false);
    }

    public BinanceConnector(String apiKey, String secretKey, boolean useUS) {
        this.apiKey = validateApiKey(apiKey);
        this.secretKey = validateSecretKey(secretKey);
        this.baseUrl = useUS ? BINANCE_US_API_URL : BINANCE_API_URL;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
    }

    /**
     * Tests connection to Binance API
     * @return true if connection is successful, false otherwise
     */
    public boolean testConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v3/ping"))
                    .timeout(REQUEST_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tests account connectivity and permissions
     * @return CompletableFuture with account information or error
     */
    public CompletableFuture<BinanceResponse> testAccountConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String endpoint = "/api/v3/account";
                long timestamp = System.currentTimeMillis();
                String queryString = "timestamp=" + timestamp;
                String signature = generateSignature(queryString);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + endpoint + "?" + queryString + "&signature=" + signature))
                        .timeout(REQUEST_TIMEOUT)
                        .header("X-MBX-APIKEY", apiKey)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                return new BinanceResponse(response.statusCode(), response.body());
            } catch (Exception e) {
                return new BinanceResponse(0, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        });
    }

    /**
     * Makes authenticated GET request to Binance API
     * @param endpoint API endpoint (e.g., "/api/v3/account")
     * @param parameters Query parameters
     * @return CompletableFuture with response
     */
    public CompletableFuture<BinanceResponse> authenticatedGet(String endpoint, Map<String, String> parameters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String queryString = buildQueryString(parameters);
                String signature = generateSignature(queryString);
                String fullUrl = baseUrl + endpoint + "?" + queryString + "&signature=" + signature;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(fullUrl))
                        .timeout(REQUEST_TIMEOUT)
                        .header("X-MBX-APIKEY", apiKey)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                return new BinanceResponse(response.statusCode(), response.body());
            } catch (Exception e) {
                return new BinanceResponse(0, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        });
    }

    /**
     * Makes authenticated POST request to Binance API
     * @param endpoint API endpoint
     * @param parameters Request parameters
     * @return CompletableFuture with response
     */
    public CompletableFuture<BinanceResponse> authenticatedPost(String endpoint, Map<String, String> parameters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String queryString = buildQueryString(parameters);
                String signature = generateSignature(queryString);
                String requestBody = queryString + "&signature=" + signature;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + endpoint))
                        .timeout(REQUEST_TIMEOUT)
                        .header("X-MBX-APIKEY", apiKey)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                return new BinanceResponse(response.statusCode(), response.body());
            } catch (Exception e) {
                return new BinanceResponse(0, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        });
    }

    /**
     * Makes public GET request (no authentication required)
     * @param endpoint API endpoint
     * @param parameters Query parameters
     * @return CompletableFuture with response
     */
    public CompletableFuture<BinanceResponse> publicGet(String endpoint, Map<String, String> parameters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String queryString = parameters != null ? buildQueryString(parameters) : "";
                String fullUrl = baseUrl + endpoint + (queryString.isEmpty() ? "" : "?" + queryString);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(fullUrl))
                        .timeout(REQUEST_TIMEOUT)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                return new BinanceResponse(response.statusCode(), response.body());
            } catch (Exception e) {
                return new BinanceResponse(0, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        });
    }

    private String generateSignature(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256Mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        sha256Mac.init(secretKeySpec);
        byte[] hash = sha256Mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    private String buildQueryString(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "timestamp=" + System.currentTimeMillis();
        }

        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(entry.getKey()).append("=").append(entry.getValue());
        }

        if (!parameters.containsKey("timestamp")) {
            queryString.append("&timestamp=").append(System.currentTimeMillis());
        }

        return queryString.toString();
    }

    private String validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        return apiKey.trim();
    }

    private String validateSecretKey(String secretKey) {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Secret key cannot be null or empty");
        }
        return secretKey.trim();
    }

    public void close() {
        // HttpClient doesn't need explicit closing in Java 11+
        // but you can add cleanup logic here if needed
    }

    // Response wrapper class
    public static class BinanceResponse {
        private final int statusCode;
        private final String body;

        public BinanceResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }

        public boolean hasError() {
            return !isSuccessful();
        }
    }
}