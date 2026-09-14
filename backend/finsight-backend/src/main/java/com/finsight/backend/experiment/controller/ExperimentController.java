package com.finsight.backend.experiment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.finsight.backend.backtest.service.BacktestService;
import com.finsight.backend.experiment.entity.Experiment;
import com.finsight.backend.experiment.service.ExperimentService;

@RestController
@RequestMapping("/api/experiments")
public class ExperimentController {

    private final ExperimentService service;

    public ExperimentController(ExperimentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Experiment> recent() {
        return service.findRecent();
    }

    @GetMapping("/{id}")
    public Experiment get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Experiment create(@RequestBody BacktestService.BacktestRequest request) {
        return service.save(request);
    }
}