package com.bbbrewery.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class DatabaseTestController {

    @Autowired
    private DataSource dataSource;

    /**
     * Test de connexion à la base de données
     * GET /api/test/db-connection
     */
    @GetMapping("/db-connection")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            // Test de connexion de base
            result.put("connected", true);
            result.put("url", connection.getMetaData().getURL());
            result.put("databaseProductName", connection.getMetaData().getDatabaseProductName());
            result.put("databaseProductVersion", connection.getMetaData().getDatabaseProductVersion());
            result.put("driverName", connection.getMetaData().getDriverName());
            result.put("driverVersion", connection.getMetaData().getDriverVersion());
            result.put("username", connection.getMetaData().getUserName());

            // Test d'une requête simple
            try (PreparedStatement stmt = connection.prepareStatement("SELECT SYSDATE FROM DUAL");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result.put("currentTime", rs.getTimestamp(1).toString());
                }
            }

            // Test de la table BB_PRODUCT (exemple)
            try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM BB_PRODUCT");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result.put("productCount", rs.getInt(1));
                    result.put("tableAccess", "BB_PRODUCT accessible");
                }
            } catch (Exception e) {
                result.put("tableTestError", "Table BB_PRODUCT: " + e.getMessage());
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("connected", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Test des tables principales
     * GET /api/test/tables
     */
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> testTables() {
        Map<String, Object> result = new HashMap<>();

        String[] tables = {"BB_PRODUCT", "BB_SHOPPER", "BB_BASKET", "BB_BASKETITEM", "BB_TAX", "BB_SHIPPING"};

        try (Connection connection = dataSource.getConnection()) {
            result.put("databaseInfo", "Connected to: " + connection.getMetaData().getURL());

            for (String table : tables) {
                try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM " + table);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        result.put(table, rs.getInt(1) + " records");
                    }
                } catch (Exception e) {
                    result.put(table, "Error: " + e.getMessage());
                }
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Test simple pour vérifier que l'API fonctionne
     * GET /api/test/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "OK");
        result.put("message", "API is running");
        result.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(result);
    }

    /**
     * Test des informations système
     * GET /api/test/system-info
     */
    @GetMapping("/system-info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> result = new HashMap<>();

        // Informations JVM
        result.put("javaVersion", System.getProperty("java.version"));
        result.put("javaVendor", System.getProperty("java.vendor"));
        result.put("osName", System.getProperty("os.name"));
        result.put("osVersion", System.getProperty("os.version"));

        // Informations mémoire
        Runtime runtime = Runtime.getRuntime();
        result.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");
        result.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
        result.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");

        return ResponseEntity.ok(result);
    }
}