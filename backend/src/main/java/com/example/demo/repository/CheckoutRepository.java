package com.example.demo.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import com.example.demo.entity.Checkout;

@Repository
public interface CheckoutRepository extends BaseRepository<Checkout, Long> {

    @Query("""
        SELECT c FROM Checkout c
        WHERE c.ativo = TRUE
        ORDER BY c.createdAt DESC
    """)
    List<Checkout> findAllAtivosOrderByCreatedAtDesc();

}
