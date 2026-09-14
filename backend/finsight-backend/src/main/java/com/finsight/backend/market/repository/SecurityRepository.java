package com.finsight.backend.market.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finsight.backend.market.entity.Security;

public interface SecurityRepository extends JpaRepository<Security, Long> {

    Optional<Security> findBySymbol(String symbol);
}