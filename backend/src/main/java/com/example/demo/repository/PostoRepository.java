package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.demo.entity.Posto;

@Repository
public interface PostoRepository extends BaseRepository<Posto, Long> {


    @Override
    @Query("""
        SELECT p FROM Posto p
        WHERE p.ativo = TRUE
        ORDER BY p.id ASC
    """)
    List<Posto> findAll();

    @Query("""
        SELECT p FROM Posto p
        ORDER BY p.id ASC
    """)
    List<Posto> findTodosOrderByIdAsc();
}
