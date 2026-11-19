package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.entities.Compte;
import com.example.demo.entities.TypeCompte;
import com.example.demo.exception.VirementRefuseException;
import com.example.demo.model.Releve;
import com.example.demo.model.VirementResultat;
import com.example.demo.repositories.CompteRepository;
import com.example.demo.repositories.MouvementRepository;
import com.example.demo.ws.VirementService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class VirementServiceTests {

    @Autowired
    private VirementService virementService;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private MouvementRepository mouvementRepository;

    private Compte source;
    private Compte destination;

    @BeforeEach
    void preparer() {
        mouvementRepository.deleteAll();
        compteRepository.deleteAll();
        source = compteRepository.save(new Compte(null, 1000.0, new Date(), TypeCompte.COURANT));
        destination = compteRepository.save(new Compte(null, 200.0, new Date(), TypeCompte.EPARGNE));
    }

    @Test
    void leVirementDebiteEtCrediteLesDeuxComptes() throws Exception {
        VirementResultat resultat = virementService.virer(source.getId(), destination.getId(), 300.0, "Loyer");

        assertThat(resultat.getSoldeSourceApres()).isEqualTo(700.0);
        assertThat(resultat.getSoldeDestinationApres()).isEqualTo(500.0);
        assertThat(compteRepository.findById(source.getId()).orElseThrow().getSolde()).isEqualTo(700.0);
        assertThat(compteRepository.findById(destination.getId()).orElseThrow().getSolde()).isEqualTo(500.0);
    }

    @Test
    void leVirementEnregistreDeuxMouvements() throws Exception {
        virementService.virer(source.getId(), destination.getId(), 100.0, "Test");

        assertThat(mouvementRepository.findByCompteIdOrderByDateOperationAsc(source.getId())).hasSize(1);
        assertThat(mouvementRepository.findByCompteIdOrderByDateOperationAsc(destination.getId())).hasSize(1);
        assertThat(mouvementRepository.findByCompteIdOrderByDateOperationAsc(source.getId()).get(0).getSens())
                .isEqualTo(com.example.demo.entities.Mouvement.Sens.DEBIT);
        assertThat(mouvementRepository.findByCompteIdOrderByDateOperationAsc(destination.getId()).get(0).getSens())
                .isEqualTo(com.example.demo.entities.Mouvement.Sens.CREDIT);
    }

    @Test
    void unVirementRefuseNeModifieAucunSolde() {
        assertThrows(VirementRefuseException.class,
                () -> virementService.virer(source.getId(), destination.getId(), 5000.0, "Trop"));

        assertThat(compteRepository.findById(source.getId()).orElseThrow().getSolde()).isEqualTo(1000.0);
        assertThat(compteRepository.findById(destination.getId()).orElseThrow().getSolde()).isEqualTo(200.0);
        assertThat(mouvementRepository.count()).isZero();
    }

    @Test
    void unMontantNulOuNegatifEstRefuse() {
        assertThrows(VirementRefuseException.class,
                () -> virementService.virer(source.getId(), destination.getId(), 0.0, "Zero"));
        assertThrows(VirementRefuseException.class,
                () -> virementService.virer(source.getId(), destination.getId(), -10.0, "Negatif"));
    }

    @Test
    void unVirementVersLeMemeCompteEstRefuse() {
        assertThrows(VirementRefuseException.class,
                () -> virementService.virer(source.getId(), source.getId(), 10.0, "Boucle"));
    }

    @Test
    void unCompteInconnuEstRefuse() {
        assertThrows(VirementRefuseException.class,
                () -> virementService.virer(999L, destination.getId(), 10.0, "Inconnu"));
    }

    @Test
    void leReleveContientLeMouvementEtLeSoldeAJour() throws Exception {
        virementService.virer(source.getId(), destination.getId(), 250.0, "Courses");

        Releve releve = virementService.releve(source.getId());

        assertThat(releve.getCompteId()).isEqualTo(source.getId());
        assertThat(releve.getSolde()).isEqualTo(750.0);
        assertThat(releve.getNombreMouvements()).isEqualTo(1);
        assertThat(releve.getMouvements().get(0).getMontant()).isEqualTo(250.0);
        assertThat(releve.getMouvements().get(0).getSoldeApres()).isEqualTo(750.0);
    }

    @Test
    void leReleveDUneCompteInconnuEstRefuse() {
        assertThrows(VirementRefuseException.class, () -> virementService.releve(999L));
    }

    @Test
    void lesSoldesRestentEquilibresApresPlusieursVirements() throws Exception {
        virementService.virer(source.getId(), destination.getId(), 100.0, "A");
        virementService.virer(destination.getId(), source.getId(), 40.0, "B");
        virementService.virer(source.getId(), destination.getId(), 60.0, "C");

        double soldeSource = compteRepository.findById(source.getId()).orElseThrow().getSolde();
        double soldeDestination = compteRepository.findById(destination.getId()).orElseThrow().getSolde();

        assertThat(soldeSource + soldeDestination).isEqualTo(1200.0);
        assertThat(mouvementRepository.count()).isEqualTo(6);
    }
}