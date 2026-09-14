package com.finsight.backend.experiment.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "experiments")
public class Experiment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;
    @Column(nullable = false)
    private LocalDate fromDate;
    @Column(nullable = false)
    private LocalDate toDate;
    @Column(nullable = false, length = 40)
    private String strategy;
    @Column(nullable = false, length = 1000)
    private String strategyParameters;
    private double initialCapital;
    private double transactionCost;
    private double slippage;
    private double finalPortfolioValue;
    private double pnl;
    private double totalReturn;
    private double volatility;
    private double maxDrawdown;
    private int tradeCount;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Experiment() {
    }

    public Experiment(String symbol, LocalDate fromDate, LocalDate toDate, String strategy,
            String strategyParameters, double initialCapital, double transactionCost, double slippage,
            double finalPortfolioValue, double pnl, double totalReturn, double volatility,
            double maxDrawdown, int tradeCount) {
        this.symbol = symbol;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.strategy = strategy;
        this.strategyParameters = strategyParameters;
        this.initialCapital = initialCapital;
        this.transactionCost = transactionCost;
        this.slippage = slippage;
        this.finalPortfolioValue = finalPortfolioValue;
        this.pnl = pnl;
        this.totalReturn = totalReturn;
        this.volatility = volatility;
        this.maxDrawdown = maxDrawdown;
        this.tradeCount = tradeCount;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getSymbol() { return symbol; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public String getStrategy() { return strategy; }
    public String getStrategyParameters() { return strategyParameters; }
    public double getInitialCapital() { return initialCapital; }
    public double getTransactionCost() { return transactionCost; }
    public double getSlippage() { return slippage; }
    public double getFinalPortfolioValue() { return finalPortfolioValue; }
    public double getPnl() { return pnl; }
    public double getTotalReturn() { return totalReturn; }
    public double getVolatility() { return volatility; }
    public double getMaxDrawdown() { return maxDrawdown; }
    public int getTradeCount() { return tradeCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}