package com.finsight.backend.backtest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.finsight.backend.backtest.service.BacktestService;

@RestController
@RequestMapping("/api/backtests")
public class BacktestController {

    private final BacktestService service;

    public BacktestController(BacktestService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public BacktestService.BacktestResult run(@RequestBody BacktestService.BacktestRequest request) {
        return service.run(request);
    }
}