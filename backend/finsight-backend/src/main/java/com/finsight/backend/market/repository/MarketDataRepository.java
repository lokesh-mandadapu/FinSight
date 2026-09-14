package com.finsight.backend.market.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finsight.backend.market.entity.MarketData;

public interface MarketDataRepository extends JpaRepository<MarketData, Long> {

    List<MarketData> findBySecuritySymbolAndTradingDateBetweenOrderByTradingDate(
            String symbol,
            LocalDate from,
            LocalDate to
    );
}