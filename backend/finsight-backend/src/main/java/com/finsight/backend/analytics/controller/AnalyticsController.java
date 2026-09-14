package com.finsight.backend.analytics.controller;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finsight.backend.analytics.service.AnalyticsService;
import com.finsight.backend.market.service.MarketDataService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final MarketDataService marketDataService;

    public AnalyticsController(AnalyticsService analyticsService, MarketDataService marketDataService) {
        this.analyticsService = analyticsService;
        this.marketDataService = marketDataService;
    }

    @GetMapping("/{symbol}")
    public AnalyticsService.AnalyticsResult getAnalytics(
            @PathVariable String symbol,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return analyticsService.calculate(marketDataService.getMarketData(symbol, from, to));
    }
}