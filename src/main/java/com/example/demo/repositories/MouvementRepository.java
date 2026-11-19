package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Mouvement;

@Repository
public interface MouvementRepository extends JpaRepository<Mouvement, Long> {

    List<Mouvement> findByCompteIdOrderByDateOperationAsc(Long compteId);

    void deleteByCompteId(Long compteId);
}