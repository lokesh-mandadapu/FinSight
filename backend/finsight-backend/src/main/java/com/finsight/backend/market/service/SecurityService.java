package com.finsight.backend.market.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.finsight.backend.market.entity.Security;
import com.finsight.backend.market.repository.SecurityRepository;

@Service
public class SecurityService {

    private final SecurityRepository repository;

    public SecurityService(SecurityRepository repository) {
        this.repository = repository;
    }

    public List<Security> findAll() {
        return repository.findAll();
    }

    public Security create(String symbol, String companyName, String exchange) {
        String normalizedSymbol = symbol.trim().toUpperCase();
        if (repository.findBySymbol(normalizedSymbol).isPresent()) {
            throw new IllegalArgumentException("Security symbol already exists: " + normalizedSymbol);
        }

        Security security = new Security(normalizedSymbol, companyName.trim(), exchange == null ? null : exchange.trim());
        try {
            return repository.save(security);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("Security symbol already exists: " + normalizedSymbol, exception);
        }
    }
}