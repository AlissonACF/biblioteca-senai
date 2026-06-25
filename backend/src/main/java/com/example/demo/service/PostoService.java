package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.dto.PostoDTO;
import com.example.demo.entity.Posto;
import com.example.demo.repository.PostoRepository;

@Service
public class PostoService extends BaseService<Posto, PostoDTO> {

    public PostoService(PostoRepository repository){
        super(repository);
    }


    @Override
    public Posto toEntity(PostoDTO dto) {
        Posto posto = super.toEntity(dto);
        if (posto.getStatus() == null) {
            posto.setStatus(com.example.demo.enums.PostoStatus.DISPONIVEL);
        }
        return posto;
    }

}
