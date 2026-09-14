package com.finsight.backend.market.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.finsight.backend.market.entity.Security;
import com.finsight.backend.market.service.SecurityService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/securities")
public class SecurityController {

    private final SecurityService service;

    public SecurityController(SecurityService service) {
        this.service = service;
    }

    @GetMapping
    public List<Security> getAllSecurities() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Security createSecurity(@Valid @RequestBody SecurityRequest request) {
        return service.create(request.symbol(), request.companyName(), request.exchange());
    }
}