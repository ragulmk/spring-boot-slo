package com.ragulmk.orders;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrdersApplicationTests {

    @Value("${local.server.port}")
    int port;

    private final HttpClient http = HttpClient.newHttpClient();

    @AfterEach
    void resetChaos() throws Exception {
        send("DELETE", "/chaos", null);
    }

    @Test
    void createAndFetchOrder() throws Exception {
        HttpResponse<String> created = send("POST", "/api/orders", "{\"item\":\"keyboard\",\"quantity\":2}");
        assertEquals(201, created.statusCode());
        assertTrue(created.body().contains("keyboard"));

        Matcher id = Pattern.compile("\"id\":(\\d+)").matcher(created.body());
        assertTrue(id.find(), "response should contain the new order id");
        assertEquals(200, send("GET", "/api/orders/" + id.group(1), null).statusCode());
        assertEquals(404, send("GET", "/api/orders/999", null).statusCode());
    }

    @Test
    void rejectsInvalidOrder() throws Exception {
        assertEquals(400, send("POST", "/api/orders", "{\"item\":\"\",\"quantity\":0}").statusCode());
    }

    @Test
    void exposesSloMetricsForPrometheus() throws Exception {
        send("GET", "/api/orders", null);
        HttpResponse<String> metrics = send("GET", "/actuator/prometheus", null);

        assertEquals(200, metrics.statusCode());
        // The SLO rules depend on these exact series existing.
        assertTrue(metrics.body().contains("http_server_requests_seconds_count"));
        assertTrue(metrics.body().contains("le=\"0.3\""), "300ms SLO bucket must be exported");
        assertTrue(metrics.body().contains("chaos_error_rate"));
    }

    @Test
    void chaosInjectsFailuresOnApiOnly() throws Exception {
        assertEquals(200, send("POST", "/chaos?errorRate=1", null).statusCode());

        assertEquals(503, send("GET", "/api/orders", null).statusCode());
        assertEquals(200, send("GET", "/actuator/health", null).statusCode());
    }

    @Test
    void healthProbesAreUp() throws Exception {
        assertEquals(200, send("GET", "/actuator/health/liveness", null).statusCode());
        assertEquals(200, send("GET", "/actuator/health/readiness", null).statusCode());
    }

    private HttpResponse<String> send(String method, String path, String json) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        HttpRequest.BodyPublisher body = json == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json);
        if (json != null) {
            builder.header("Content-Type", "application/json");
        }
        return http.send(builder.method(method, body).build(), HttpResponse.BodyHandlers.ofString());
    }
}
