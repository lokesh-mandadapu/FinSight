package com.finsight.backend.experiment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finsight.backend.experiment.entity.Experiment;

public interface ExperimentRepository extends JpaRepository<Experiment, Long> {
    List<Experiment> findTop20ByOrderByCreatedAtDesc();
}