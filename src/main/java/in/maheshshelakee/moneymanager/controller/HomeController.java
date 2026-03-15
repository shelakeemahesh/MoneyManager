package in.maheshshelakee.moneymanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight health-check endpoint. Whitelisted in SecurityConfig (/health, /status).
 * Returns JSON (not plain String) for compatibility with load balancer checks.
 */
@RestController
public class HomeController {

    @GetMapping({"/health", "/status"})
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
