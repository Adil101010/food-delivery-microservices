package com.fooddelivery.analyticsservice.service;

import com.fooddelivery.analyticsservice.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    @PersistenceContext
    private EntityManager entityManager;

    // Dashboard Stats
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // Total counts
        stats.setTotalOrders(getTotalOrders());
        stats.setTotalUsers(getTotalUsers());
        stats.setTotalRestaurants(getTotalRestaurants());
        stats.setTotalDeliveries(getTotalDeliveries());

        // Revenue stats
        stats.setTotalRevenue(getTotalRevenue());
        stats.setTodayRevenue(getTodayRevenue());
        stats.setMonthRevenue(getMonthRevenue());

        // Order stats
        stats.setPendingOrders(getOrdersByStatus("PENDING"));
        stats.setCompletedOrders(getOrdersByStatus("DELIVERED"));
        stats.setCancelledOrders(getOrdersByStatus("CANCELLED"));

        // Average stats
        stats.setAverageOrderValue(getAverageOrderValue());
        stats.setAverageRating(getAverageRating());

        // Active stats
        stats.setActiveDeliveryPartners(getActiveDeliveryPartners());
        stats.setActiveRestaurants(getActiveRestaurants());

        return stats;
    }

    // Order Analytics
    public OrderAnalytics getOrderAnalytics() {
        OrderAnalytics analytics = new OrderAnalytics();

        analytics.setTotalOrders(getTotalOrders());
        analytics.setTotalRevenue(getTotalRevenue());
        analytics.setAverageOrderValue(getAverageOrderValue());

        // Orders by status
        Map<String, Long> ordersByStatus = new HashMap<>();
        ordersByStatus.put("PENDING", getOrdersByStatus("PENDING"));
        ordersByStatus.put("CONFIRMED", getOrdersByStatus("CONFIRMED"));
        ordersByStatus.put("PREPARING", getOrdersByStatus("PREPARING"));
        ordersByStatus.put("READY", getOrdersByStatus("READY"));
        ordersByStatus.put("PICKED_UP", getOrdersByStatus("PICKED_UP"));
        ordersByStatus.put("DELIVERED", getOrdersByStatus("DELIVERED"));
        ordersByStatus.put("CANCELLED", getOrdersByStatus("CANCELLED"));
        analytics.setOrdersByStatus(ordersByStatus);

        // Orders by payment method
        Map<String, Long> ordersByPayment = new HashMap<>();
        ordersByPayment.put("CARD", getOrdersByPaymentMethod("CREDIT_CARD"));
        ordersByPayment.put("UPI", getOrdersByPaymentMethod("UPI"));
        ordersByPayment.put("CASH", getOrdersByPaymentMethod("CASH_ON_DELIVERY"));
        ordersByPayment.put("WALLET", getOrdersByPaymentMethod("WALLET"));
        analytics.setOrdersByPaymentMethod(ordersByPayment);

        // Time-based orders
        analytics.setTodayOrders(getTodayOrders());
        analytics.setWeekOrders(getWeekOrders());
        analytics.setMonthOrders(getMonthOrders());

        return analytics;
    }

    // Restaurant Analytics
    public List<RestaurantAnalytics> getRestaurantAnalytics() {
        String query = """
            SELECT r.id, r.name, 
                   COUNT(o.id) as totalOrders,
                   COALESCE(SUM(o.total_amount), 0) as totalRevenue,
                   COALESCE(AVG(rev.rating), 0) as avgRating,
                   COUNT(rev.id) as totalReviews,
                   CASE WHEN r.is_open = 1 THEN 'OPEN' ELSE 'CLOSED' END as status
            FROM restaurants r
            LEFT JOIN orders o ON r.id = o.restaurant_id
            LEFT JOIN reviews rev ON r.id = rev.restaurant_id
            GROUP BY r.id, r.name, r.is_open
            ORDER BY totalRevenue DESC
            """;

        List<Object[]> results = entityManager.createNativeQuery(query).getResultList();

        return results.stream().map(row -> {
            RestaurantAnalytics analytics = new RestaurantAnalytics();
            analytics.setRestaurantId(((Number) row[0]).longValue());
            analytics.setRestaurantName((String) row[1]);
            analytics.setTotalOrders(((Number) row[2]).longValue());
            // Fix BigDecimal conversion
            Object revenueObj = row[3];
            if (revenueObj instanceof BigDecimal) {
                analytics.setTotalRevenue((BigDecimal) revenueObj);
            } else {
                analytics.setTotalRevenue(BigDecimal.valueOf(((Number) revenueObj).doubleValue()));
            }
            analytics.setAverageRating(((Number) row[4]).doubleValue());
            analytics.setTotalReviews(((Number) row[5]).longValue());
            analytics.setStatus((String) row[6]);
            return analytics;
        }).toList();
    }

    // User Analytics
    public UserAnalytics getUserAnalytics() {
        UserAnalytics analytics = new UserAnalytics();

        analytics.setTotalUsers(getTotalUsers());
        analytics.setActiveUsers(getActiveUsers());
        analytics.setNewUsersToday(getNewUsersToday());
        analytics.setNewUsersThisWeek(getNewUsersThisWeek());
        analytics.setNewUsersThisMonth(getNewUsersThisMonth());

        // Top customer
        String topCustomerQuery = """
            SELECT u.id, u.name, COUNT(o.id) as orderCount, 
                   COALESCE(SUM(o.total_amount), 0) as totalSpent
            FROM users u
            LEFT JOIN orders o ON u.id = o.user_id
            GROUP BY u.id, u.name
            ORDER BY totalSpent DESC
            LIMIT 1
            """;

        List<Object[]> topCustomer = entityManager.createNativeQuery(topCustomerQuery).getResultList();
        if (!topCustomer.isEmpty()) {
            Object[] row = topCustomer.get(0);
            analytics.setTopCustomerId(((Number) row[0]).longValue());
            analytics.setTopCustomerName((String) row[1]);
            analytics.setTopCustomerOrders(((Number) row[2]).longValue());
            // Fix BigDecimal conversion
            Object spendingObj = row[3];
            if (spendingObj instanceof BigDecimal) {
                analytics.setTopCustomerSpending((BigDecimal) spendingObj);
            } else {
                analytics.setTopCustomerSpending(BigDecimal.valueOf(((Number) spendingObj).doubleValue()));
            }
        }

        return analytics;
    }

    // Delivery Analytics
    public DeliveryAnalytics getDeliveryAnalytics() {
        DeliveryAnalytics analytics = new DeliveryAnalytics();

        analytics.setTotalDeliveries(getTotalDeliveries());
        analytics.setTotalPartners(getTotalDeliveryPartners());
        analytics.setActivePartners(getActiveDeliveryPartners());

        // Deliveries by status
        Map<String, Long> deliveriesByStatus = new HashMap<>();
        deliveriesByStatus.put("ASSIGNED", getDeliveriesByStatus("ASSIGNED"));
        deliveriesByStatus.put("PICKED_UP", getDeliveriesByStatus("ACCEPTED"));
        deliveriesByStatus.put("IN_TRANSIT", getDeliveriesByStatus("ACCEPTED"));
        deliveriesByStatus.put("DELIVERED", getDeliveriesByStatus("DELIVERED"));
        analytics.setDeliveriesByStatus(deliveriesByStatus);

        analytics.setAverageDeliveryTime(getAverageDeliveryTime());
        analytics.setAveragePartnerRating(getAveragePartnerRating());

        // Top delivery partner
        String topPartnerQuery = """
            SELECT dp.id, COUNT(d.id) as deliveryCount, 
                   COALESCE(AVG(dr.rating), 0) as avgRating
            FROM delivery_partners dp
            LEFT JOIN deliveries d ON dp.id = d.partner_id
            LEFT JOIN delivery_ratings dr ON dp.id = dr.delivery_partner_id
            GROUP BY dp.id
            ORDER BY deliveryCount DESC
            LIMIT 1
            """;

        List<Object[]> topPartner = entityManager.createNativeQuery(topPartnerQuery).getResultList();
        if (!topPartner.isEmpty()) {
            Object[] row = topPartner.get(0);
            analytics.setTopPartnerId(((Number) row[0]).longValue());
            analytics.setTopPartnerDeliveries(((Number) row[1]).intValue());
            analytics.setTopPartnerRating(((Number) row[2]).doubleValue());
        }

        return analytics;
    }

    // Revenue Analytics
    public RevenueAnalytics getRevenueAnalytics() {
        RevenueAnalytics analytics = new RevenueAnalytics();

        analytics.setTotalRevenue(getTotalRevenue());
        analytics.setTodayRevenue(getTodayRevenue());
        analytics.setWeekRevenue(getWeekRevenue());
        analytics.setMonthRevenue(getMonthRevenue());

        // Revenue by payment method
        Map<String, BigDecimal> revenueByPayment = new HashMap<>();
        revenueByPayment.put("CARD", getRevenueByPaymentMethod("CREDIT_CARD"));
        revenueByPayment.put("UPI", getRevenueByPaymentMethod("UPI"));
        revenueByPayment.put("CASH", getRevenueByPaymentMethod("CASH_ON_DELIVERY"));
        revenueByPayment.put("WALLET", getRevenueByPaymentMethod("WALLET"));
        analytics.setRevenueByPaymentMethod(revenueByPayment);

        analytics.setFoodRevenue(getFoodRevenue());
        analytics.setDeliveryRevenue(getDeliveryRevenue());
        analytics.setDiscountGiven(getTotalDiscount());

        analytics.setAverageDailyRevenue(getAverageDailyRevenue());
        analytics.setHighestOrderValue(getHighestOrderValue());
        analytics.setLowestOrderValue(getLowestOrderValue());

        return analytics;
    }

    // Helper Methods
    private Long getTotalOrders() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM orders").getSingleResult()).longValue();
    }

    private Long getTotalUsers() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM users").getSingleResult()).longValue();
    }

    private Long getTotalRestaurants() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM restaurants").getSingleResult()).longValue();
    }

    private Long getTotalDeliveries() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM deliveries").getSingleResult()).longValue();
    }

    private Long getTotalDeliveryPartners() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM delivery_partners").getSingleResult()).longValue();
    }

    private BigDecimal getTotalRevenue() {
        Object result = entityManager.createNativeQuery("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE order_status = 'DELIVERED'").getSingleResult();
        if (result == null) return BigDecimal.ZERO;
        if (result instanceof BigDecimal) return (BigDecimal) result;
        return BigDecimal.valueOf(((Number) result).doubleValue());
    }

    private BigDecimal getTodayRevenue() {
        String query = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE order_status = 'DELIVERED' AND DATE(created_at) = CURDATE()";
        Object result = entityManager.createNativeQuery(query).getSingleResult();
        if (result == null) return BigDecimal.ZERO;
        if (result instanceof BigDecimal) return (BigDecimal) result;
        return BigDecimal.valueOf(((Number) result).doubleValue());
    }

    private BigDecimal getWeekRevenue() {
        String query = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE order_status = 'DELIVERED' AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
        Object result = entityManager.createNativeQuery(query).getSingleResult();
        if (result == null) return BigDecimal.ZERO;
        if (result instanceof BigDecimal) return (BigDecimal) result;
        return BigDecimal.valueOf(((Number) result).doubleValue());
    }

    private BigDecimal getMonthRevenue() {
        String query = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE order_status = 'DELIVERED' AND MONTH(created_at) = MONTH(NOW()) AND YEAR(created_at) = YEAR(NOW())";
        Object result = entityManager.createNativeQuery(query).getSingleResult();
        if (result == null) return BigDecimal.ZERO;
        if (result instanceof BigDecimal) return (BigDecimal) result;
        return BigDecimal.valueOf(((Number) result).doubleValue());
    }

    private Long getOrdersByStatus(String orderStatus) {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM orders WHERE order_status = :status")
                .setParameter("status", orderStatus).getSingleResult()).longValue();
    }

    private Long getOrdersByPaymentMethod(String method) {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM payments WHERE payment_method = :method")
                .setParameter("method", method).getSingleResult()).longValue();
    }

    private Long getTodayOrders() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURDATE()").getSingleResult()).longValue();
    }

    private Long getWeekOrders() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM orders WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)").getSingleResult()).longValue();
    }

    private Long getMonthOrders() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM orders WHERE MONTH(created_at) = MONTH(NOW()) AND YEAR(created_at) = YEAR(NOW())").getSingleResult()).longValue();
    }

    private BigDecimal getAverageOrderValue() {
        Object result = entityManager.createNativeQuery("SELECT COALESCE(AVG(total_amount), 0) FROM orders WHERE order_status = 'DELIVERED'").getSingleResult();
        if (result == null) return BigDecimal.ZERO;
        if (result instanceof BigDecimal) {
            return ((BigDecimal) result).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(((Number) result).doubleValue()).setScale(2, RoundingMode.HALF_UP);
    }

    private Double getAverageRating() {
        Object result = entityManager.createNativeQuery("SELECT COALESCE(AVG(rating), 0) FROM reviews").getSingleResult();
        return result != null ? ((Number) result).doubleValue() : 0.0;
    }

    private Long getActiveDeliveryPartners() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM delivery_partners WHERE status = 'ACTIVE'").getSingleResult()).longValue();
    }

    private Long getActiveRestaurants() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM restaurants WHERE is_open = 1 AND is_active = 1").getSingleResult()).longValue();
    }

    private Long getActiveUsers() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(DISTINCT user_id) FROM orders WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)").getSingleResult()).longValue();
    }

    private Long getNewUsersToday() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM users WHERE DATE(created_at) = CURDATE()").getSingleResult()).longValue();
    }

    private Long getNewUsersThisWeek() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM users WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)").getSingleResult()).longValue();
    }

    private Long getNewUsersThisMonth() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM users WHERE MONTH(created_at) = MONTH(NOW()) AND YEAR(created_at) = YEAR(NOW())").getSingleResult()).longValue();
    }

    private Long getDeliveriesByStatus(String deliveryStatus) {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM deliveries WHERE status = :status")
                .setParameter("status", deliveryStatus).getSingleResult()).longValue();
    }

    private Double getAverageDeliveryTime() {
        Object result = entityManager.createNativeQuery("SELECT COALESCE(AVG(TIMESTAMPDIFF(MINUTE, picked_up_at, delivered_at)), 0) FROM deliveries WHERE status = 'DELIVERED'").getSingleResult();
        return result != null ? ((Number) result).doubleValue() : 0.0;
    }

    private Double getAveragePartnerRating() {
        Object result = entityManager.createNativeQuery("SELECT COALESCE(AVG(rating), 0) FROM delivery_ratings").getSingleResult();
        return result != null ? ((Number) result).doubleValue() : 0.0;
    }

    private BigDecimal getRevenueByPaymentMethod(String method) {
        String query = "SELECT COALESCE(SUM(o.total_amount), 0) FROM orders o JOIN payments p ON o.id = p.order_id WHERE p.payment_method = :method AND p.status = 'COMPLETED'";
        Object result = entityManager.createNativeQuery(query).setParameter("method", method).getSingleResult();
        if (result == null) return BigDecimal.ZERO;
        if (result instanceof BigDecimal) return (BigDecimal) result;
        return BigDecimal.valueOf(((Number) result).doubleValue());
    }

    private BigDecimal getFoodRevenue() {
        Object result = entityManager.createNativeQuery("SELECT COALESCE(SUM(subtotal), 0) FROM orders WHERE order_status = 'DELIVERED'").getSingleResult();
        if (result == null) return BigDecimal.ZERO;
        if (result instanceof BigDecimal) return (BigDecimal) result;
        return BigDecimal.valueOf(((Number) result).doubleValue());
    }

    private BigDecimal getDeliveryRevenue() {
        Object result = entityManager.createNativeQuery("SELECT COALESCE(SUM(delivery_fee), 0) FROM orders WHERE order_status = 'DELIVERED'").getSingleResult();
        if (result == null) return BigDecimal.ZERO;
        if (result instanceof BigDecimal) return (BigDecimal) result;
        return BigDecimal.valueOf(((Number) result).doubleValue());
    }

    private BigDecimal getTotalDiscount() {
        Object result = entityManager.createNativeQuery("SELECT COALESCE(SUM(discount), 0) FROM orders WHERE order_status = 'DELIVERED'").getSingleResult();
        if (result == null) return BigDecimal.ZERO;
        if (result instanceof BigDecimal) return (BigDecimal) result;
        return BigDecimal.valueOf(((Number) result).doubleValue());
    }

    private BigDecimal getAverageDailyRevenue() {
        String query = "SELECT COALESCE(AVG(daily_revenue), 0) FROM (SELECT DATE(created_at) as order_date, SUM(total_amount) as daily_revenue FROM orders WHERE order_status = 'DELIVERED' GROUP BY DATE(created_at)) as daily_stats";
        Object result = entityManager.createNativeQuery(query).getSingleResult();
        if (result == null) return BigDecimal.ZERO;
        if (result instanceof BigDecimal) {
            return ((BigDecimal) result).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(((Number) result).doubleValue()).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getHighestOrderValue() {
        Object result = entityManager.createNativeQuery("SELECT COALESCE(MAX(total_amount), 0) FROM orders WHERE order_status = 'DELIVERED'").getSingleResult();
        if (result == null) return BigDecimal.ZERO;
        if (result instanceof BigDecimal) return (BigDecimal) result;
        return BigDecimal.valueOf(((Number) result).doubleValue());
    }

    private BigDecimal getLowestOrderValue() {
        Object result = entityManager.createNativeQuery("SELECT COALESCE(MIN(total_amount), 0) FROM orders WHERE order_status = 'DELIVERED'").getSingleResult();
        if (result == null) return BigDecimal.ZERO;
        if (result instanceof BigDecimal) return (BigDecimal) result;
        return BigDecimal.valueOf(((Number) result).doubleValue());
    }
}
