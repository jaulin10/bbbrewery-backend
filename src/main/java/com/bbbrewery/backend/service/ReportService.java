package com.bbbrewery.backend.service;

import com.bbbrewery.backend.repository.ReportRepository;
import com.bbbrewery.backend.repository.ReportRepository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    /**
     * Génère le rapport de stock - utilise la méthode du repository
     */
    public List<StockReportItem> generateStockReport() {
        return reportRepository.getStockReport();
    }

    /**
     * Génère le rapport des achats par client
     */
    public List<PurchaseReportItem> getPurchaseReport(Long shopperId,
                                                      LocalDateTime startDate,
                                                      LocalDateTime endDate) {
        return reportRepository.getPurchaseReport(shopperId, startDate, endDate);
    }

    /**
     * Génère le rapport des ventes par produit
     */
    public List<ProductSalesReportItem> getProductSalesReport(LocalDateTime startDate,
                                                              LocalDateTime endDate) {
        return reportRepository.getProductSalesReport(startDate, endDate);
    }

    /**
     * Génère le rapport des revenus par période
     */
    public List<RevenueReportItem> getRevenueReport(String period,
                                                    LocalDateTime startDate,
                                                    LocalDateTime endDate) {
        return reportRepository.getRevenueReport(period, startDate, endDate);
    }

    /**
     * Génère le rapport des taxes collectées
     */
    public List<TaxReportItem> getTaxReport(LocalDateTime startDate, LocalDateTime endDate) {
        return reportRepository.getTaxReport(startDate, endDate);
    }

    /**
     * Récupère les données du tableau de bord
     */
    public DashboardData getDashboardData() {
        return reportRepository.getDashboardData();
    }

    /**
     * Récupère les produits avec stock faible (utilise le dashboard pour optimiser)
     */
    public List<StockReportItem> getLowStockProducts(int threshold) {
        // Utilise le rapport de stock et filtre
        return reportRepository.getStockReport().stream()
                .filter(item -> item.getCurrentStock() <= threshold)
                .toList();
    }

    /**
     * Récupère les statistiques de ventes par période en utilisant le rapport de revenus
     */
    public Map<String, Object> getSalesStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        // Utilise le rapport de revenus quotidien pour calculer les statistiques
        List<RevenueReportItem> dailyRevenues = reportRepository.getRevenueReport("DAILY", startDate, endDate);

        Map<String, Object> statistics = new HashMap<>();

        int totalOrders = dailyRevenues.stream()
                .mapToInt(RevenueReportItem::getOrderCount)
                .sum();

        BigDecimal totalRevenue = dailyRevenues.stream()
                .map(RevenueReportItem::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTax = dailyRevenues.stream()
                .map(RevenueReportItem::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalShipping = dailyRevenues.stream()
                .map(RevenueReportItem::getShippingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        statistics.put("totalOrders", totalOrders);
        statistics.put("totalRevenue", totalRevenue);
        statistics.put("totalTaxCollected", totalTax);
        statistics.put("totalShippingRevenue", totalShipping);

        // Panier moyen
        if (totalOrders > 0) {
            BigDecimal averageBasket = totalRevenue.divide(
                    BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP);
            statistics.put("averageBasketSize", averageBasket);
        } else {
            statistics.put("averageBasketSize", BigDecimal.ZERO);
        }

        return statistics;
    }

    /**
     * Récupère les ventes mensuelles pour une année
     */
    public List<RevenueReportItem> getMonthlySalesReport(int year) {
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        return reportRepository.getRevenueReport("MONTHLY", startDate, endDate);
    }

    /**
     * Récupère les ventes quotidiennes
     */
    public List<RevenueReportItem> getDailyRevenueReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return reportRepository.getRevenueReport("DAILY", start, end);
    }

    /**
     * Récupère les clients les plus actifs (basé sur le rapport d'achats)
     */
    public List<PurchaseReportItem> getTopCustomersReport(int limit) {
        List<PurchaseReportItem> allCustomers = reportRepository.getPurchaseReport(null, null, null);
        return allCustomers.stream()
                .sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()))
                .limit(limit)
                .toList();
    }

    /**
     * Récupère les produits les plus vendus
     */
    public List<ProductSalesReportItem> getBestSellingProducts(int limit) {
        List<ProductSalesReportItem> allProducts = reportRepository.getProductSalesReport(null, null);
        return allProducts.stream()
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .limit(limit)
                .toList();
    }

    /**
     * Génère le tableau de bord executif complet
     */
    public Map<String, Object> getExecutiveDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // Utilise les données du dashboard du repository
        DashboardData dashboardData = reportRepository.getDashboardData();
        dashboard.put("dashboardData", dashboardData);

        // Statistiques du mois en cours
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> monthlyStats = getSalesStatistics(startOfMonth, now);
        dashboard.put("currentMonthStats", monthlyStats);

        // Produits en stock faible
        List<StockReportItem> lowStock = getLowStockProducts(10);
        dashboard.put("lowStockProducts", lowStock.size());
        dashboard.put("lowStockList", lowStock);

        return dashboard;
    }

    /**
     * Récupère les tendances de ventes (utilise les données de revenus mensuels)
     */
    public Map<String, Object> getSalesTrends(int months) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(months);

        List<RevenueReportItem> monthlyData = reportRepository.getRevenueReport("MONTHLY", startDate, endDate);

        Map<String, Object> trends = new HashMap<>();
        trends.put("monthlyRevenue", monthlyData);

        // Calcul de la croissance
        if (monthlyData.size() >= 2) {
            BigDecimal lastMonth = monthlyData.get(monthlyData.size() - 1).getTotalRevenue();
            BigDecimal previousMonth = monthlyData.get(monthlyData.size() - 2).getTotalRevenue();

            if (previousMonth.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal growthRate = lastMonth.subtract(previousMonth)
                        .divide(previousMonth, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                trends.put("monthlyGrowthRate", growthRate);
            }
        }

        return trends;
    }

    /**
     * Exporte les données de ventes au format CSV (utilise le rapport de revenus)
     */
    public String exportSalesToCSV(LocalDateTime startDate, LocalDateTime endDate) {
        List<RevenueReportItem> dailyRevenues = reportRepository.getRevenueReport("DAILY", startDate, endDate);

        StringBuilder csv = new StringBuilder();
        csv.append("Date,Order Count,Subtotal,Tax,Shipping,Total Revenue\n");

        for (RevenueReportItem item : dailyRevenues) {
            csv.append(item.getPeriod()).append(",")
                    .append(item.getOrderCount()).append(",")
                    .append(item.getSubtotal()).append(",")
                    .append(item.getTaxAmount()).append(",")
                    .append(item.getShippingAmount()).append(",")
                    .append(item.getTotalRevenue()).append("\n");
        }

        return csv.toString();
    }

    /**
     * Récupère un résumé rapide des KPIs
     */
    public Map<String, Object> getKPISummary() {
        DashboardData dashboardData = reportRepository.getDashboardData();

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("activeProducts", dashboardData.getActiveProducts());
        kpis.put("totalCustomers", dashboardData.getTotalCustomers());
        kpis.put("activeBaskets", dashboardData.getActiveBaskets());
        kpis.put("ordersToday", dashboardData.getOrdersToday());
        kpis.put("revenueToday", dashboardData.getRevenueToday());
        kpis.put("revenueMonth", dashboardData.getRevenueMonth());
        kpis.put("lowStockProductCount", dashboardData.getLowStockProducts());

        return kpis;
    }
}