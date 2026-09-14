package com.finsight.backend.backtest.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.finsight.backend.analytics.service.AnalyticsService;
import com.finsight.backend.market.entity.MarketData;
import com.finsight.backend.market.service.MarketDataService;

@Service
public class BacktestService {

    private final MarketDataService marketDataService;
    private final AnalyticsService analyticsService;

    public BacktestService(MarketDataService marketDataService, AnalyticsService analyticsService) {
        this.marketDataService = marketDataService;
        this.analyticsService = analyticsService;
    }

    public BacktestResult run(BacktestRequest request) {
        List<MarketData> data = marketDataService.getMarketData(request.symbol(), request.from(), request.to());
        if (data.isEmpty()) {
            throw new IllegalArgumentException("No market data found for the requested range");
        }

        double feeRate = Math.max(0, request.transactionCost()) / 100;
        double slippageRate = Math.max(0, request.slippage()) / 100;
        double cash = request.initialCapital();
        double shares = 0;
        int tradeCount = 0;
        List<EquityPoint> equityCurve = new ArrayList<>();
        List<Trade> trades = new ArrayList<>();

        for (int index = 0; index < data.size(); index++) {
            MarketData point = data.get(index);
            double price = point.getClose();
            boolean shouldHold = request.strategy().equalsIgnoreCase("BUY_AND_HOLD")
                    ? index == 0
                    : index >= request.longSma() - 1 && sma(data, index, request.shortSma()) > sma(data, index, request.longSma());
            boolean currentlyHolding = shares > 0;

            if (shouldHold && !currentlyHolding) {
                double executionPrice = price * (1 + slippageRate);
                shares = cash / (executionPrice * (1 + feeRate));
                cash -= shares * executionPrice * (1 + feeRate);
                tradeCount++;
                trades.add(new Trade(point.getTradingDate(), "BUY", shares, executionPrice));
            } else if (!shouldHold && currentlyHolding && !request.strategy().equalsIgnoreCase("BUY_AND_HOLD")) {
                double executionPrice = price * (1 - slippageRate);
                cash += shares * executionPrice * (1 - feeRate);
                trades.add(new Trade(point.getTradingDate(), "SELL", shares, executionPrice));
                shares = 0;
                tradeCount++;
            }
            equityCurve.add(new EquityPoint(point.getTradingDate(), cash + shares * price));
        }

        AnalyticsService.AnalyticsResult metrics = analyticsFromEquity(equityCurve);
        double finalValue = equityCurve.get(equityCurve.size() - 1).value();
        return new BacktestResult(request.symbol(), request.from(), request.to(), request.strategy(),
                request.shortSma(), request.longSma(), request.initialCapital(), request.transactionCost(), request.slippage(),
                finalValue, finalValue - request.initialCapital(), finalValue / request.initialCapital() - 1,
                metrics.volatility(), metrics.maxDrawdown(), tradeCount, equityCurve, trades);
    }

    private double sma(List<MarketData> data, int end, int period) {
        if (period <= 0 || end + 1 < period) {
            return 0;
        }
        return data.subList(end - period + 1, end + 1).stream().mapToDouble(MarketData::getClose).average().orElse(0);
    }

    private AnalyticsService.AnalyticsResult analyticsFromEquity(List<EquityPoint> curve) {
        List<MarketData> synthetic = new ArrayList<>();
        for (EquityPoint point : curve) {
            MarketData data = new MarketData();
            data.setTradingDate(point.date());
            data.setClose(point.value());
            synthetic.add(data);
        }
        return analyticsService.calculate(synthetic);
    }

    public record BacktestRequest(String symbol, LocalDate from, LocalDate to, String strategy,
            int shortSma, int longSma, double initialCapital, double transactionCost, double slippage) {
        public BacktestRequest {
            strategy = strategy == null ? "BUY_AND_HOLD" : strategy;
            shortSma = shortSma <= 0 ? 20 : shortSma;
            longSma = longSma <= 0 ? 50 : longSma;
            initialCapital = initialCapital <= 0 ? 100000 : initialCapital;
        }
    }

    public record BacktestResult(String symbol, LocalDate from, LocalDate to, String strategy,
            int shortSma, int longSma, double initialCapital, double transactionCost, double slippage,
            double finalPortfolioValue, double pnl, double totalReturn, double volatility,
            double maxDrawdown, int tradeCount, List<EquityPoint> equityCurve, List<Trade> trades) {
    }

    public record EquityPoint(LocalDate date, double value) {
    }

    public record Trade(LocalDate date, String side, double quantity, double price) {
    }
}