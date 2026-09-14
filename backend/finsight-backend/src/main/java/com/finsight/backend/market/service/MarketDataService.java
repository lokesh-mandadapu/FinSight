package com.finsight.backend.market.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.finsight.backend.market.entity.MarketData;
import com.finsight.backend.market.entity.Security;
import com.finsight.backend.market.repository.MarketDataRepository;
import com.finsight.backend.market.repository.SecurityRepository;

@Service
public class MarketDataService {

    private final MarketDataRepository repository;
    private final SecurityRepository securityRepository;

    public MarketDataService(
            MarketDataRepository repository,
            SecurityRepository securityRepository) {

        this.repository = repository;
        this.securityRepository = securityRepository;
    }

    public List<MarketData> getMarketData(
            String symbol,
            LocalDate from,
            LocalDate to) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol is required");
        }
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("from must be on or before to");
        }
        if (securityRepository.findBySymbol(symbol).isEmpty()) {
            throw new IllegalArgumentException("Security not found: " + symbol);
        }

        return repository
                .findBySecuritySymbolAndTradingDateBetweenOrderByTradingDate(
                        symbol, from, to);
    }

    public MarketData saveMarketData(MarketData marketData) {
        return repository.save(marketData);
    }

    public void generateSampleData(Long securityId) {

        Security security = securityRepository.findById(securityId)
            .orElseThrow(() -> new IllegalArgumentException("Security not found: " + securityId));

        double price = 1400.0;

        for (int i = 0; i < 30; i++) {

            LocalDate date =
                    LocalDate.of(2026, 8, 1).plusDays(i);

            if (date.getDayOfWeek().getValue() >= 6) {
                continue;
            }

            double open = price;
            double close = price + ((i % 5) - 2) * 8;
            double high = Math.max(open, close) + 10;
            double low = Math.min(open, close) - 10;

            MarketData data = new MarketData();

            data.setSecurity(security);
            data.setTradingDate(date);
            data.setOpen(open);
            data.setHigh(high);
            data.setLow(low);
            data.setClose(close);
            data.setVolume(1000000L + (i * 25000L));

            repository.save(data);

            price = close;
        }
    }
}