package com.finsight.backend.experiment.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.finsight.backend.backtest.service.BacktestService;
import com.finsight.backend.experiment.entity.Experiment;
import com.finsight.backend.experiment.repository.ExperimentRepository;

@Service
public class ExperimentService {

    private final ExperimentRepository repository;
    private final BacktestService backtestService;

    public ExperimentService(ExperimentRepository repository, BacktestService backtestService) {
        this.repository = repository;
        this.backtestService = backtestService;
    }

    public Experiment save(BacktestService.BacktestRequest request) {
        BacktestService.BacktestResult result = backtestService.run(request);
        String parameters = "shortSma=" + result.shortSma() + ", longSma=" + result.longSma();
        return repository.save(new Experiment(result.symbol(), result.from(), result.to(), result.strategy(), parameters,
                result.initialCapital(), result.transactionCost(), result.slippage(), result.finalPortfolioValue(),
                result.pnl(), result.totalReturn(), result.volatility(), result.maxDrawdown(), result.tradeCount()));
    }

    public List<Experiment> findRecent() {
        return repository.findTop20ByOrderByCreatedAtDesc();
    }

    public Experiment findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Experiment not found: " + id));
    }
}