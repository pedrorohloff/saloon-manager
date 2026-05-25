package com.example.services;

import java.util.Map;

public record BusinessDashboardStats(
        double weeklyRevenue,
        Map<String, Long> serviceCounts,
        double monthlyProjection
) {
}
