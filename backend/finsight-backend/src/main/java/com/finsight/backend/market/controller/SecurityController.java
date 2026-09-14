package com.finsight.backend.market.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finsight.backend.market.entity.Security;
import com.finsight.backend.market.repository.SecurityRepository;

@RestController
@RequestMapping("/api/securities")
public class SecurityController {

    private final SecurityRepository repository;

    public SecurityController(SecurityRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Security> getAllSecurities() {
        return repository.findAll();
    }

    @PostMapping
    public Security createSecurity(@RequestBody Security security) {
        return repository.save(security);
    }
}