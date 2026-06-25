package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CheckinDTO;
import com.example.demo.dto.CheckinResponseDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Posto;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.PostoRepository;

@Service
public class CheckService {

    @Autowired
    private PostoRepository postoRepository;

    @Autowired
    private ArquivoService arquivoService;

    @Autowired
    private CheckinRepository checkinRepository;

    public CheckinResponseDTO checkin(CheckinDTO dto){
        Posto posto = postoRepository.findById(dto.getPostoId()).orElseThrow();

        Checkin checkin = new Checkin();

        checkin.setPosto(posto);
        checkin.setGuardaVidasEmail(getUsuarioAutenticado());

        Arquivo arquivo = arquivoService.upload(dto.getFoto());

        checkin.setFoto(arquivo);

        Checkin checkinSalvo = checkinRepository.save(checkin);      

        CheckinResponseDTO crd = new CheckinResponseDTO();

        crd.setPosto(posto.getNome());
        crd.setHorario(checkinSalvo.getCreatedAt());

        return crd;
    }


    private String getUsuarioAutenticado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return authentication.getName();
    }
}
