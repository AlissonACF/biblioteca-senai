package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.CheckinRelatorioDTO;
import com.example.demo.dto.CheckoutRelatorioDTO;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Checkout;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.CheckoutRepository;

@Service
public class RelatorioService {

    private final CheckinRepository checkinRepository;
    private final CheckoutRepository checkoutRepository;

    public RelatorioService(CheckinRepository checkinRepository, CheckoutRepository checkoutRepository) {
        this.checkinRepository = checkinRepository;
        this.checkoutRepository = checkoutRepository;
    }

    public List<CheckinRelatorioDTO> listarCheckins() {
        return checkinRepository.findAllAtivosOrderByCreatedAtDesc().stream()
                .map(this::toCheckinRelatorio)
                .toList();
    }

    public List<CheckoutRelatorioDTO> listarCheckouts() {
        return checkoutRepository.findAllAtivosOrderByCreatedAtDesc().stream()
                .map(this::toCheckoutRelatorio)
                .toList();
    }

    private CheckinRelatorioDTO toCheckinRelatorio(Checkin checkin) {
        CheckinRelatorioDTO dto = new CheckinRelatorioDTO();
        dto.setId(checkin.getId());
        dto.setHorario(checkin.getCreatedAt());
        dto.setGuardaVidasEmail(valorOuNaoInformado(checkin.getGuardaVidasEmail()));

        if (checkin.getPosto() != null) {
            dto.setPostoId(checkin.getPosto().getId());
            dto.setPostoNome(checkin.getPosto().getNome());
        }

        if (checkin.getFoto() != null) {
            dto.setFotoNome(checkin.getFoto().getNome());
        }

        return dto;
    }

    private CheckoutRelatorioDTO toCheckoutRelatorio(Checkout checkout) {
        CheckoutRelatorioDTO dto = new CheckoutRelatorioDTO();
        dto.setId(checkout.getId());
        dto.setHorario(checkout.getCreatedAt());
        dto.setPrevencoes(checkout.getPrevencoes());
        dto.setLesoes(checkout.getLesoes());
        dto.setGuardaVidasEmail(valorOuNaoInformado(checkout.getGuardaVidasEmail()));

        if (checkout.getPosto() != null) {
            dto.setPostoId(checkout.getPosto().getId());
            dto.setPostoNome(checkout.getPosto().getNome());
        }

        if (checkout.getFoto() != null) {
            dto.setFotoNome(checkout.getFoto().getNome());
        }

        return dto;
    }

    private String valorOuNaoInformado(String valor) {
        return valor == null || valor.isBlank() ? "Nao informado" : valor;
    }
}
