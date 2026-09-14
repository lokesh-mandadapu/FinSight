package com.finsight.backend.analytics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.finsight.backend.market.entity.MarketData;

class AnalyticsServiceTest {

    private final AnalyticsService service = new AnalyticsService();

    @Test
    void calculatesReturnAndDrawdownFromClosingPrices() {
        List<MarketData> data = List.of(point(100), point(110), point(99), point(121));

        AnalyticsService.AnalyticsResult result = service.calculate(data);

        assertEquals(0.21, result.totalReturn(), 0.000001);
        assertEquals(-0.1, result.maxDrawdown(), 0.000001);
        assertEquals(3, result.dailyReturns().size());
    }

    @Test
    void handlesInsufficientDataWithoutInventingMetrics() {
        AnalyticsService.AnalyticsResult result = service.calculate(List.of(point(100)));

        assertEquals(0, result.totalReturn());
        assertEquals(0, result.volatility());
        assertEquals(0, result.maxDrawdown());
    }

    private MarketData point(double close) {
        MarketData data = new MarketData();
        data.setClose(close);
        return data;
    }
}