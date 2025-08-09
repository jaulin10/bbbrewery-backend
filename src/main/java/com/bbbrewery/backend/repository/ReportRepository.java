package com.bbbrewery.backend.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ReportRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Rapport de stock
    public List<StockReportItem> getStockReport() {
        String sql = """
            SELECT p.IDPRODUCT, p.PRODUCTNAME, p.STOCK, p.PRICE, 
                   COALESCE(sales.TOTAL_SOLD, 0) as TOTAL_SOLD,
                   p.STOCK * p.PRICE as STOCK_VALUE
            FROM BB_PRODUCT p
            LEFT JOIN (
                SELECT bi.IDPRODUCT, SUM(bi.QUANTITY) as TOTAL_SOLD
                FROM BB_BASKETITEM bi
                JOIN BB_BASKET b ON bi.IDBASKET = b.IDBASKET
                WHERE b.ORDERPLACED > 1
                GROUP BY bi.IDPRODUCT
            ) sales ON p.IDPRODUCT = sales.IDPRODUCT
            WHERE p.ACTIVE = 1
            ORDER BY p.PRODUCTNAME
            """;

        return jdbcTemplate.query(sql, new StockReportRowMapper());
    }

    // Rapport des achats par client
    public List<PurchaseReportItem> getPurchaseReport(Long shopperId,
                                                      LocalDateTime startDate,
                                                      LocalDateTime endDate) {
        String sql = """
            SELECT s.IDSHOPPER, s.FIRSTNAME, s.LASTNAME, s.EMAIL,
                   COUNT(b.IDBASKET) as ORDER_COUNT,
                   SUM(b.TOTAL) as TOTAL_SPENT,
                   AVG(b.TOTAL) as AVERAGE_ORDER_VALUE,
                   MAX(b.DTORDERED) as LAST_ORDER_DATE
            FROM BB_SHOPPER s
            JOIN BB_BASKET b ON s.IDSHOPPER = b.IDSHOPPER
            WHERE b.ORDERPLACED > 1
            """;

        Object[] params;
        if (shopperId != null) {
            sql += " AND s.IDSHOPPER = ?";
            if (startDate != null && endDate != null) {
                sql += " AND b.DTORDERED BETWEEN ? AND ?";
                params = new Object[]{shopperId, startDate, endDate};
            } else {
                params = new Object[]{shopperId};
            }
        } else if (startDate != null && endDate != null) {
            sql += " AND b.DTORDERED BETWEEN ? AND ?";
            params = new Object[]{startDate, endDate};
        } else {
            params = new Object[]{};
        }

        sql += " GROUP BY s.IDSHOPPER, s.FIRSTNAME, s.LASTNAME, s.EMAIL ORDER BY TOTAL_SPENT DESC";

        return jdbcTemplate.query(sql, params, new PurchaseReportRowMapper());
    }

    // Rapport des ventes par produit
    public List<ProductSalesReportItem> getProductSalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT p.IDPRODUCT, p.PRODUCTNAME, 
                   SUM(bi.QUANTITY) as QUANTITY_SOLD,
                   SUM(bi.QUANTITY * bi.PRICE) as REVENUE,
                   AVG(bi.PRICE) as AVERAGE_PRICE,
                   COUNT(DISTINCT b.IDSHOPPER) as UNIQUE_CUSTOMERS
            FROM BB_PRODUCT p
            JOIN BB_BASKETITEM bi ON p.IDPRODUCT = bi.IDPRODUCT
            JOIN BB_BASKET b ON bi.IDBASKET = b.IDBASKET
            WHERE b.ORDERPLACED > 1
            """;

        Object[] params;
        if (startDate != null && endDate != null) {
            sql += " AND b.DTORDERED BETWEEN ? AND ?";
            params = new Object[]{startDate, endDate};
        } else {
            params = new Object[]{};
        }

        sql += " GROUP BY p.IDPRODUCT, p.PRODUCTNAME ORDER BY REVENUE DESC";

        return jdbcTemplate.query(sql, params, new ProductSalesReportRowMapper());
    }

    // Rapport des revenus par période
    public List<RevenueReportItem> getRevenueReport(String period, LocalDateTime startDate, LocalDateTime endDate) {
        String dateFormat = switch (period.toUpperCase()) {
            case "DAILY" -> "TO_CHAR(b.DTORDERED, 'YYYY-MM-DD')";
            case "WEEKLY" -> "TO_CHAR(b.DTORDERED, 'YYYY-IW')";
            case "MONTHLY" -> "TO_CHAR(b.DTORDERED, 'YYYY-MM')";
            case "YEARLY" -> "TO_CHAR(b.DTORDERED, 'YYYY')";
            default -> "TO_CHAR(b.DTORDERED, 'YYYY-MM-DD')";
        };

        String sql = String.format("""
            SELECT %s as PERIOD,
                   COUNT(b.IDBASKET) as ORDER_COUNT,
                   SUM(b.SUBTOTAL) as SUBTOTAL,
                   SUM(b.TAX) as TAX_AMOUNT,
                   SUM(b.SHIPPING) as SHIPPING_AMOUNT,
                   SUM(b.TOTAL) as TOTAL_REVENUE
            FROM BB_BASKET b
            WHERE b.ORDERPLACED > 1
            AND b.DTORDERED BETWEEN ? AND ?
            GROUP BY %s
            ORDER BY %s
            """, dateFormat, dateFormat, dateFormat);

        return jdbcTemplate.query(sql, new Object[]{startDate, endDate}, new RevenueReportRowMapper());
    }

    // Rapport des taxes collectées
    public List<TaxReportItem> getTaxReport(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT t.TAXTYPE,
                   CASE t.TAXTYPE
                       WHEN 1 THEN 'Sales Tax'
                       WHEN 2 THEN 'VAT'
                       WHEN 3 THEN 'GST'
                       WHEN 4 THEN 'PST'
                       WHEN 5 THEN 'HST'
                       ELSE 'Other'
                   END as TAX_TYPE_NAME,
                   t.STATE, t.PROVINCE,
                   COUNT(t.IDTAX) as TAX_RECORDS,
                   SUM(t.TAXAMOUNT) as TOTAL_TAX_COLLECTED,
                   AVG(t.TAXRATE) as AVERAGE_TAX_RATE
            FROM BB_TAX t
            JOIN BB_BASKET b ON t.IDBASKET = b.IDBASKET
            WHERE b.ORDERPLACED > 1
            AND b.DTORDERED BETWEEN ? AND ?
            GROUP BY t.TAXTYPE, t.STATE, t.PROVINCE
            ORDER BY TOTAL_TAX_COLLECTED DESC
            """;

        return jdbcTemplate.query(sql, new Object[]{startDate, endDate}, new TaxReportRowMapper());
    }

    // Données du tableau de bord
    public DashboardData getDashboardData() {
        String sql = """
            SELECT 
                (SELECT COUNT(*) FROM BB_PRODUCT WHERE ACTIVE = 1) as ACTIVE_PRODUCTS,
                (SELECT COUNT(*) FROM BB_SHOPPER) as TOTAL_CUSTOMERS,
                (SELECT COUNT(*) FROM BB_BASKET WHERE ORDERPLACED = 1) as ACTIVE_BASKETS,
                (SELECT COUNT(*) FROM BB_BASKET WHERE ORDERPLACED > 1 AND DTORDERED >= TRUNC(SYSDATE)) as ORDERS_TODAY,
                (SELECT NVL(SUM(TOTAL), 0) FROM BB_BASKET WHERE ORDERPLACED > 1 AND DTORDERED >= TRUNC(SYSDATE)) as REVENUE_TODAY,
                (SELECT NVL(SUM(TOTAL), 0) FROM BB_BASKET WHERE ORDERPLACED > 1 AND DTORDERED >= TRUNC(SYSDATE, 'MM')) as REVENUE_MONTH,
                (SELECT COUNT(*) FROM BB_PRODUCT WHERE STOCK <= 5 AND ACTIVE = 1) as LOW_STOCK_PRODUCTS
            FROM DUAL
            """;

        return jdbcTemplate.queryForObject(sql, new DashboardDataRowMapper());
    }

    // Classes internes pour les Row Mappers et Data Classes
    public static class StockReportItem {
        public Long productId;
        public String productName;
        public Integer currentStock;
        public BigDecimal price;
        public Integer totalSold;
        public BigDecimal stockValue;

        // Getters et setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getCurrentStock() { return currentStock; }
        public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getTotalSold() { return totalSold; }
        public void setTotalSold(Integer totalSold) { this.totalSold = totalSold; }
        public BigDecimal getStockValue() { return stockValue; }
        public void setStockValue(BigDecimal stockValue) { this.stockValue = stockValue; }
    }

    public static class PurchaseReportItem {
        public Long shopperId;
        public String firstName;
        public String lastName;
        public String email;
        public Integer orderCount;
        public BigDecimal totalSpent;
        public BigDecimal averageOrderValue;
        public LocalDateTime lastOrderDate;

        // Getters et setters
        public Long getShopperId() { return shopperId; }
        public void setShopperId(Long shopperId) { this.shopperId = shopperId; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
        public BigDecimal getTotalSpent() { return totalSpent; }
        public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
        public BigDecimal getAverageOrderValue() { return averageOrderValue; }
        public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }
        public LocalDateTime getLastOrderDate() { return lastOrderDate; }
        public void setLastOrderDate(LocalDateTime lastOrderDate) { this.lastOrderDate = lastOrderDate; }
    }

    public static class ProductSalesReportItem {
        public Long productId;
        public String productName;
        public Integer quantitySold;
        public BigDecimal revenue;
        public BigDecimal averagePrice;
        public Integer uniqueCustomers;

        // Getters et setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantitySold() { return quantitySold; }
        public void setQuantitySold(Integer quantitySold) { this.quantitySold = quantitySold; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
        public BigDecimal getAveragePrice() { return averagePrice; }
        public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }
        public Integer getUniqueCustomers() { return uniqueCustomers; }
        public void setUniqueCustomers(Integer uniqueCustomers) { this.uniqueCustomers = uniqueCustomers; }
    }

    public static class RevenueReportItem {
        public String period;
        public Integer orderCount;
        public BigDecimal subtotal;
        public BigDecimal taxAmount;
        public BigDecimal shippingAmount;
        public BigDecimal totalRevenue;

        // Getters et setters
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
        public BigDecimal getTaxAmount() { return taxAmount; }
        public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
        public BigDecimal getShippingAmount() { return shippingAmount; }
        public void setShippingAmount(BigDecimal shippingAmount) { this.shippingAmount = shippingAmount; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    }

    public static class TaxReportItem {
        public Integer taxType;
        public String taxTypeName;
        public String state;
        public String province;
        public Integer taxRecords;
        public BigDecimal totalTaxCollected;
        public BigDecimal averageTaxRate;

        // Getters et setters
        public Integer getTaxType() { return taxType; }
        public void setTaxType(Integer taxType) { this.taxType = taxType; }
        public String getTaxTypeName() { return taxTypeName; }
        public void setTaxTypeName(String taxTypeName) { this.taxTypeName = taxTypeName; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public Integer getTaxRecords() { return taxRecords; }
        public void setTaxRecords(Integer taxRecords) { this.taxRecords = taxRecords; }
        public BigDecimal getTotalTaxCollected() { return totalTaxCollected; }
        public void setTotalTaxCollected(BigDecimal totalTaxCollected) { this.totalTaxCollected = totalTaxCollected; }
        public BigDecimal getAverageTaxRate() { return averageTaxRate; }
        public void setAverageTaxRate(BigDecimal averageTaxRate) { this.averageTaxRate = averageTaxRate; }
    }

    public static class DashboardData {
        public Integer activeProducts;
        public Integer totalCustomers;
        public Integer activeBaskets;
        public Integer ordersToday;
        public BigDecimal revenueToday;
        public BigDecimal revenueMonth;
        public Integer lowStockProducts;

        // Getters et setters
        public Integer getActiveProducts() { return activeProducts; }
        public void setActiveProducts(Integer activeProducts) { this.activeProducts = activeProducts; }
        public Integer getTotalCustomers() { return totalCustomers; }
        public void setTotalCustomers(Integer totalCustomers) { this.totalCustomers = totalCustomers; }
        public Integer getActiveBaskets() { return activeBaskets; }
        public void setActiveBaskets(Integer activeBaskets) { this.activeBaskets = activeBaskets; }
        public Integer getOrdersToday() { return ordersToday; }
        public void setOrdersToday(Integer ordersToday) { this.ordersToday = ordersToday; }
        public BigDecimal getRevenueToday() { return revenueToday; }
        public void setRevenueToday(BigDecimal revenueToday) { this.revenueToday = revenueToday; }
        public BigDecimal getRevenueMonth() { return revenueMonth; }
        public void setRevenueMonth(BigDecimal revenueMonth) { this.revenueMonth = revenueMonth; }
        public Integer getLowStockProducts() { return lowStockProducts; }
        public void setLowStockProducts(Integer lowStockProducts) { this.lowStockProducts = lowStockProducts; }
    }

    // Row Mappers
    private static class StockReportRowMapper implements RowMapper<StockReportItem> {
        @Override
        public StockReportItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            StockReportItem item = new StockReportItem();
            item.productId = rs.getLong("IDPRODUCT");
            item.productName = rs.getString("PRODUCTNAME");
            item.currentStock = rs.getInt("STOCK");
            item.price = rs.getBigDecimal("PRICE");
            item.totalSold = rs.getInt("TOTAL_SOLD");
            item.stockValue = rs.getBigDecimal("STOCK_VALUE");
            return item;
        }
    }

    private static class PurchaseReportRowMapper implements RowMapper<PurchaseReportItem> {
        @Override
        public PurchaseReportItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            PurchaseReportItem item = new PurchaseReportItem();
            item.shopperId = rs.getLong("IDSHOPPER");
            item.firstName = rs.getString("FIRSTNAME");
            item.lastName = rs.getString("LASTNAME");
            item.email = rs.getString("EMAIL");
            item.orderCount = rs.getInt("ORDER_COUNT");
            item.totalSpent = rs.getBigDecimal("TOTAL_SPENT");
            item.averageOrderValue = rs.getBigDecimal("AVERAGE_ORDER_VALUE");
            item.lastOrderDate = rs.getTimestamp("LAST_ORDER_DATE") != null
                    ? rs.getTimestamp("LAST_ORDER_DATE").toLocalDateTime() : null;
            return item;
        }
    }

    private static class ProductSalesReportRowMapper implements RowMapper<ProductSalesReportItem> {
        @Override
        public ProductSalesReportItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            ProductSalesReportItem item = new ProductSalesReportItem();
            item.productId = rs.getLong("IDPRODUCT");
            item.productName = rs.getString("PRODUCTNAME");
            item.quantitySold = rs.getInt("QUANTITY_SOLD");
            item.revenue = rs.getBigDecimal("REVENUE");
            item.averagePrice = rs.getBigDecimal("AVERAGE_PRICE");
            item.uniqueCustomers = rs.getInt("UNIQUE_CUSTOMERS");
            return item;
        }
    }

    private static class RevenueReportRowMapper implements RowMapper<RevenueReportItem> {
        @Override
        public RevenueReportItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            RevenueReportItem item = new RevenueReportItem();
            item.period = rs.getString("PERIOD");
            item.orderCount = rs.getInt("ORDER_COUNT");
            item.subtotal = rs.getBigDecimal("SUBTOTAL");
            item.taxAmount = rs.getBigDecimal("TAX_AMOUNT");
            item.shippingAmount = rs.getBigDecimal("SHIPPING_AMOUNT");
            item.totalRevenue = rs.getBigDecimal("TOTAL_REVENUE");
            return item;
        }
    }

    private static class TaxReportRowMapper implements RowMapper<TaxReportItem> {
        @Override
        public TaxReportItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            TaxReportItem item = new TaxReportItem();
            item.taxType = rs.getInt("TAXTYPE");
            item.taxTypeName = rs.getString("TAX_TYPE_NAME");
            item.state = rs.getString("STATE");
            item.province = rs.getString("PROVINCE");
            item.taxRecords = rs.getInt("TAX_RECORDS");
            item.totalTaxCollected = rs.getBigDecimal("TOTAL_TAX_COLLECTED");
            item.averageTaxRate = rs.getBigDecimal("AVERAGE_TAX_RATE");
            return item;
        }
    }

    private static class DashboardDataRowMapper implements RowMapper<DashboardData> {
        @Override
        public DashboardData mapRow(ResultSet rs, int rowNum) throws SQLException {
            DashboardData data = new DashboardData();
            data.activeProducts = rs.getInt("ACTIVE_PRODUCTS");
            data.totalCustomers = rs.getInt("TOTAL_CUSTOMERS");
            data.activeBaskets = rs.getInt("ACTIVE_BASKETS");
            data.ordersToday = rs.getInt("ORDERS_TODAY");
            data.revenueToday = rs.getBigDecimal("REVENUE_TODAY");
            data.revenueMonth = rs.getBigDecimal("REVENUE_MONTH");
            data.lowStockProducts = rs.getInt("LOW_STOCK_PRODUCTS");
            return data;
        }
    }
}