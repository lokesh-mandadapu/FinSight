package com.finsight.backend.market.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finsight.backend.market.entity.MarketData;
import com.finsight.backend.market.service.MarketDataService;

@RestController
@RequestMapping("/api/market-data")
public class MarketDataController {

    private final MarketDataService service;

    public MarketDataController(MarketDataService service) {
        this.service = service;
    }

    @GetMapping("/{symbol}")
    public List<MarketData> getMarketData(
            @PathVariable String symbol,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {

        return service.getMarketData(symbol, from, to);
    }

    @PostMapping
    public MarketData createMarketData(@RequestBody MarketData marketData) {

        return service.saveMarketData(marketData);
    }

    @PostMapping("/generate/{securityId}")
    public String generateSampleData(@PathVariable Long securityId) {

    service.generateSampleData(securityId);

    return "Sample market data generated";
}
}