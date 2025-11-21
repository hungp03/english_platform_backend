package com.english.api.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DashboardStatsRepositoryCustom {

    private final EntityManager em;

    public Map<String, Long> getAllDashboardStats(
            OffsetDateTime weekAgo,
            Instant sevenDaysAgo,
            Instant threeDaysAgo,
            OffsetDateTime startOfMonth,
            OffsetDateTime endOfMonth,
            OffsetDateTime startPrevMonth,
            OffsetDateTime endPrevMonth
    ) {
        String sql = "SELECT * FROM get_all_dashboard_stats(:weekAgo, :sevenDaysAgo, :threeDaysAgo, :startOfMonth, :endOfMonth, :startPrevMonth, :endPrevMonth)";

        Query query = em.createNativeQuery(sql);
        query.setParameter("weekAgo", weekAgo);
        query.setParameter("sevenDaysAgo", sevenDaysAgo);
        query.setParameter("threeDaysAgo", threeDaysAgo);
        query.setParameter("startOfMonth", startOfMonth);
        query.setParameter("endOfMonth", endOfMonth);
        query.setParameter("startPrevMonth", startPrevMonth);
        query.setParameter("endPrevMonth", endPrevMonth);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue(),
                        (v1, v2) -> v1
                ));
    }
}