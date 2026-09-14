package com.finsight.backend.portfolio.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    @PostMapping("/valuation")
    public PortfolioValuation value(@RequestBody PortfolioRequest request) {
        double marketValue = request.quantity() * request.currentPrice();
        double pnl = request.quantity() * (request.currentPrice() - request.averagePrice());
        double totalValue = request.cash() + marketValue;
        return new PortfolioValuation(request.symbol(), request.cash(), request.quantity(), request.averagePrice(),
                request.currentPrice(), marketValue, pnl, totalValue,
                totalValue == 0 ? 0 : marketValue / totalValue);
    }

    public record PortfolioRequest(String symbol, double cash, double quantity, double averagePrice, double currentPrice) {
    }

    public record PortfolioValuation(String symbol, double cash, double quantity, double averagePrice,
            double currentPrice, double marketValue, double unrealizedPnl, double portfolioValue, double weight) {
    }
}