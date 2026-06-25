package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.Admin;
import com.example.demo.dto.CheckinRelatorioDTO;
import com.example.demo.dto.CheckoutRelatorioDTO;
import com.example.demo.service.RelatorioService;

@Admin
@RestController
@RequestMapping("/admin/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/checkins")
    public List<CheckinRelatorioDTO> listarCheckins() {
        return relatorioService.listarCheckins();
    }

    @GetMapping("/checkouts")
    public List<CheckoutRelatorioDTO> listarCheckouts() {
        return relatorioService.listarCheckouts();
    }
}
