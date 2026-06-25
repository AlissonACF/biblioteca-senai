package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CheckoutDTO;
import com.example.demo.service.CheckoutService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/checkout")
public class CheckoutController extends BaseController<CheckoutDTO> {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService service) {
        super(service);
        this.checkoutService = service;
    }

    @PostMapping(value = "/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CheckoutDTO registrar(@Valid CheckoutDTO dto) {
        return checkoutService.registrar(dto);
    }
}
