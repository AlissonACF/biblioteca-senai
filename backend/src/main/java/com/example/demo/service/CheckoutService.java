package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.dto.CheckoutDTO;
import com.example.demo.entity.Checkout;
import com.example.demo.entity.Posto;
import com.example.demo.repository.CheckoutRepository;
import com.example.demo.repository.PostoRepository;

@Service
public class CheckoutService extends BaseService<Checkout, CheckoutDTO> {

    private final CheckoutRepository checkoutRepository;
    private final PostoRepository postoRepository;
    private final ArquivoService arquivoService;

    public CheckoutService(CheckoutRepository repository, PostoRepository postoRepository, ArquivoService arquivoService) {
        super(repository);
        this.checkoutRepository = repository;
        this.postoRepository = postoRepository;
        this.arquivoService = arquivoService;
    }

    public CheckoutDTO registrar(CheckoutDTO dto) {
        Posto posto = postoRepository.findById(dto.getPostoId()).orElseThrow();

        Checkout checkout = new Checkout();
        checkout.setPosto(posto);
        checkout.setGuardaVidasEmail(getUsuarioAutenticado());
        checkout.setPrevencoes(dto.getPrevencoes());
        checkout.setLesoes(dto.getLesoes());

        if (dto.getFoto() != null && !dto.getFoto().isEmpty()) {
            checkout.setFoto(arquivoService.upload(dto.getFoto()));
        }

        return toDto(checkoutRepository.save(checkout));
    }

    @Override
    public CheckoutDTO create(CheckoutDTO dto) {
        return registrar(dto);
    }

    @Override
    public CheckoutDTO toDto(Checkout checkout) {
        CheckoutDTO dto = new CheckoutDTO();
        dto.setId(checkout.getId());
        dto.setPrevencoes(checkout.getPrevencoes());
        dto.setLesoes(checkout.getLesoes());
        if (checkout.getPosto() != null) {
            dto.setPostoId(checkout.getPosto().getId());
        }
        return dto;
    }

    private String getUsuarioAutenticado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return authentication.getName();
    }
}
