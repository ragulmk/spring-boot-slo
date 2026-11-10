package com.ragulmk.orders.chaos;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/chaos")
public class ChaosController {

    private final ChaosSettings settings;

    public ChaosController(ChaosSettings settings) {
        this.settings = settings;
    }

    @GetMapping
    public Map<String, Object> status() {
        return Map.of("errorRate", settings.errorRate(), "latencyMs", settings.latencyMs());
    }

    @PostMapping
    public Map<String, Object> set(@RequestParam(defaultValue = "0") double errorRate,
                                   @RequestParam(defaultValue = "0") long latencyMs) {
        settings.update(errorRate, latencyMs);
        return status();
    }

    @DeleteMapping
    public Map<String, Object> reset() {
        settings.update(0, 0);
        return status();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }
}
