package com.bbbrewery.backend.controller;

import com.bbbrewery.backend.service.ReportService;
import com.bbbrewery.backend.repository.ReportRepository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200"}, allowCredentials = "false")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Génère le rapport de stock
     * GET /api/reports/stock
     */
    @GetMapping("/stock")
    public ResponseEntity<List<StockReportItem>> getStockReport() {
        try {
            List<StockReportItem> report = reportService.generateStockReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Génère le rapport des achats par client
     * GET /api/reports/purchases
     */
    @GetMapping("/purchases")
    public ResponseEntity<List<PurchaseReportItem>> getPurchaseReport(
            @RequestParam(required = false) Long shopperId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

            List<PurchaseReportItem> report = reportService.getPurchaseReport(shopperId, start, end);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Génère le rapport des ventes par produit
     * GET /api/reports/product-sales
     */
    @GetMapping("/product-sales")
    public ResponseEntity<List<ProductSalesReportItem>> getProductSalesReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

            List<ProductSalesReportItem> report = reportService.getProductSalesReport(start, end);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Génère le rapport des revenus par période
     * GET /api/reports/revenue
     */
    @GetMapping("/revenue")
    public ResponseEntity<List<RevenueReportItem>> getRevenueReport(
            @RequestParam(defaultValue = "DAILY") String period,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            List<RevenueReportItem> report = reportService.getRevenueReport(period, start, end);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Génère le rapport des taxes collectées
     * GET /api/reports/taxes
     */
    @GetMapping("/taxes")
    public ResponseEntity<List<TaxReportItem>> getTaxReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            List<TaxReportItem> report = reportService.getTaxReport(start, end);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Récupère les données du tableau de bord
     * GET /api/reports/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardData> getDashboardData() {
        try {
            DashboardData dashboardData = reportService.getDashboardData();
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les produits avec stock faible
     * GET /api/reports/low-stock
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<StockReportItem>> getLowStockProducts(
            @RequestParam(defaultValue = "5") int threshold) {
        try {
            List<StockReportItem> products = reportService.getLowStockProducts(threshold);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les statistiques de ventes par période
     * GET /api/reports/sales-statistics
     */
    @GetMapping("/sales-statistics")
    public ResponseEntity<Map<String, Object>> getSalesStatistics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            Map<String, Object> statistics = reportService.getSalesStatistics(start, end);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Récupère les ventes mensuelles pour une année
     * GET /api/reports/monthly-sales/{year}
     */
    @GetMapping("/monthly-sales/{year}")
    public ResponseEntity<List<RevenueReportItem>> getMonthlySalesReport(@PathVariable int year) {
        try {
            List<RevenueReportItem> report = reportService.getMonthlySalesReport(year);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère le rapport des clients les plus actifs
     * GET /api/reports/top-customers
     */
    @GetMapping("/top-customers")
    public ResponseEntity<List<PurchaseReportItem>> getTopCustomersReport(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<PurchaseReportItem> report = reportService.getTopCustomersReport(limit);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les produits les plus vendus
     * GET /api/reports/best-selling-products
     */
    @GetMapping("/best-selling-products")
    public ResponseEntity<List<ProductSalesReportItem>> getBestSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ProductSalesReportItem> report = reportService.getBestSellingProducts(limit);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Génère le rapport des revenus quotidiens
     * GET /api/reports/daily-revenue
     */
    @GetMapping("/daily-revenue")
    public ResponseEntity<List<RevenueReportItem>> getDailyRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<RevenueReportItem> report = reportService.getDailyRevenueReport(start, end);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Génère le tableau de bord exécutif
     * GET /api/reports/executive-dashboard
     */
    @GetMapping("/executive-dashboard")
    public ResponseEntity<Map<String, Object>> getExecutiveDashboard() {
        try {
            Map<String, Object> dashboard = reportService.getExecutiveDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les tendances de ventes
     * GET /api/reports/sales-trends
     */
    @GetMapping("/sales-trends")
    public ResponseEntity<Map<String, Object>> getSalesTrends(
            @RequestParam(defaultValue = "12") int months) {
        try {
            Map<String, Object> trends = reportService.getSalesTrends(months);
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporte les données de ventes au format CSV
     * GET /api/reports/export/sales-csv
     */
    @GetMapping("/export/sales-csv")
    public ResponseEntity<String> exportSalesToCSV(@RequestParam String startDate,
                                                   @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            String csvData = reportService.exportSalesToCSV(start, end);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "sales-report.csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Récupère un résumé rapide des KPIs
     * GET /api/reports/kpi-summary
     */
    @GetMapping("/kpi-summary")
    public ResponseEntity<Map<String, Object>> getKPISummary() {
        try {
            Map<String, Object> kpis = reportService.getKPISummary();
            return ResponseEntity.ok(kpis);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTS SUPPLÉMENTAIRES POUR EXPLOITER TOUTES LES FONCTIONNALITÉS ==========

    /**
     * Récupère un client spécifique avec ses achats
     * GET /api/reports/customer/{shopperId}
     */
    @GetMapping("/customer/{shopperId}")
    public ResponseEntity<List<PurchaseReportItem>> getCustomerPurchases(@PathVariable Long shopperId) {
        try {
            List<PurchaseReportItem> report = reportService.getPurchaseReport(shopperId, null, null);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les revenus hebdomadaires
     * GET /api/reports/weekly-revenue
     */
    @GetMapping("/weekly-revenue")
    public ResponseEntity<List<RevenueReportItem>> getWeeklyRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            List<RevenueReportItem> report = reportService.getRevenueReport("WEEKLY", start, end);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Récupère les revenus annuels
     * GET /api/reports/yearly-revenue
     */
    @GetMapping("/yearly-revenue")
    public ResponseEntity<List<RevenueReportItem>> getYearlyRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            List<RevenueReportItem> report = reportService.getRevenueReport("YEARLY", start, end);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Récupère les ventes d'un produit spécifique
     * GET /api/reports/product/{productId}/sales
     */
    @GetMapping("/product/{productId}/sales")
    public ResponseEntity<List<ProductSalesReportItem>> getProductSales(
            @PathVariable Long productId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

            List<ProductSalesReportItem> allProducts = reportService.getProductSalesReport(start, end);
            List<ProductSalesReportItem> productSales = allProducts.stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .toList();

            return ResponseEntity.ok(productSales);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Récupère les taxes par type
     * GET /api/reports/taxes/by-type
     */
    @GetMapping("/taxes/by-type")
    public ResponseEntity<List<TaxReportItem>> getTaxReportByType(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Integer taxType) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            List<TaxReportItem> allTaxes = reportService.getTaxReport(start, end);

            if (taxType != null) {
                List<TaxReportItem> filteredTaxes = allTaxes.stream()
                        .filter(tax -> tax.getTaxType().equals(taxType))
                        .toList();
                return ResponseEntity.ok(filteredTaxes);
            }

            return ResponseEntity.ok(allTaxes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Récupère un résumé rapide pour aujourd'hui
     * GET /api/reports/today-summary
     */
    @GetMapping("/today-summary")
    public ResponseEntity<Map<String, Object>> getTodaySummary() {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime now = LocalDateTime.now();

            Map<String, Object> todayStats = reportService.getSalesStatistics(startOfDay, now);
            return ResponseEntity.ok(todayStats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}