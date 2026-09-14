package com.finsight.backend.analytics.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.finsight.backend.market.entity.MarketData;

@Service
public class AnalyticsService {

    public AnalyticsResult calculate(List<MarketData> data) {
        if (data == null || data.size() < 2) {
            return new AnalyticsResult(0, 0, 0, 0, 0, 0, List.of());
        }

        List<Double> returns = dailyReturns(data);
        double totalReturn = data.get(0).getClose() == 0
                ? 0
                : (data.get(data.size() - 1).getClose() / data.get(0).getClose()) - 1;
        double volatility = annualizedVolatility(returns);
        double maxDrawdown = maximumDrawdown(data);
        double averageReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double sharpe = volatility == 0 ? 0 : averageReturn * Math.sqrt(252) / volatility;
        double downside = Math.sqrt(returns.stream().mapToDouble(value -> Math.pow(Math.min(value, 0), 2)).average().orElse(0));
        double sortino = downside == 0 ? 0 : averageReturn * Math.sqrt(252) / downside;

        return new AnalyticsResult(totalReturn, volatility, maxDrawdown, sharpe, sortino,
                historicalVar(returns, 0.05), returns);
    }

    public List<Double> dailyReturns(List<MarketData> data) {
        List<Double> returns = new ArrayList<>();
        for (int index = 1; index < data.size(); index++) {
            double previous = data.get(index - 1).getClose();
            returns.add(previous == 0 ? 0 : data.get(index).getClose() / previous - 1);
        }
        return returns;
    }

    public double annualizedVolatility(List<Double> returns) {
        if (returns.size() < 2) {
            return 0;
        }
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = returns.stream().mapToDouble(value -> Math.pow(value - mean, 2)).sum() / (returns.size() - 1);
        return Math.sqrt(variance) * Math.sqrt(252);
    }

    public double maximumDrawdown(List<MarketData> data) {
        double peak = 0;
        double maxDrawdown = 0;
        for (MarketData point : data) {
            peak = Math.max(peak, point.getClose());
            if (peak > 0) {
                maxDrawdown = Math.min(maxDrawdown, point.getClose() / peak - 1);
            }
        }
        return maxDrawdown;
    }

    private double historicalVar(List<Double> returns, double percentile) {
        if (returns.isEmpty()) {
            return 0;
        }
        List<Double> sorted = new ArrayList<>(returns);
        Collections.sort(sorted);
        int index = Math.max(0, (int) Math.floor((sorted.size() - 1) * percentile));
        return -sorted.get(index);
    }

    public record AnalyticsResult(
            double totalReturn,
            double volatility,
            double maxDrawdown,
            double sharpeRatio,
            double sortinoRatio,
            double valueAtRisk,
            List<Double> dailyReturns) {
    }
}