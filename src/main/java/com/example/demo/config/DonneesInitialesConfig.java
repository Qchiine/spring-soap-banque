package com.example.demo.config;

import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.example.demo.entities.Compte;
import com.example.demo.entities.TypeCompte;
import com.example.demo.repositories.CompteRepository;

/**
 * Jeu de donnees initial, desactive pendant les tests pour que les assertions
 * portent sur un etat vide connu.
 */
@Configuration
@Profile("!test")
public class DonneesInitialesConfig {

    @Bean
    CommandLineRunner initialiserComptes(CompteRepository compteRepository) {
        return args -> {
            if (compteRepository.count() > 0) {
                return;
            }
            compteRepository.save(new Compte(null, 4500.00, new Date(), TypeCompte.EPARGNE));
            compteRepository.save(new Compte(null, 1200.00, new Date(), TypeCompte.COURANT));
            compteRepository.save(new Compte(null, 8750.50, new Date(), TypeCompte.COURANT));
        };
    }
}
