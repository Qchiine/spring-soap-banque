package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.entities.Compte;
import com.example.demo.entities.TypeCompte;
import com.example.demo.exception.CompteIntrouvableException;
import com.example.demo.model.ComptesResponse;
import com.example.demo.repositories.CompteRepository;
import com.example.demo.repositories.MouvementRepository;
import com.example.demo.ws.CompteSoapService;
import com.example.demo.ws.VirementService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CompteSoapServiceTests {

    @Autowired
    private CompteSoapService service;

    @Autowired
    private CompteRepository repository;

    @Autowired
    private MouvementRepository mouvementRepository;

    @Autowired
    private VirementService virementService;

    @BeforeEach
    void vider() {
        mouvementRepository.deleteAll();
        repository.deleteAll();
    }

    @Test
    void createComptePersisteEtRetourneLIdentifiant() {
        Compte compte = service.createCompte(1500.50, TypeCompte.EPARGNE);

        assertThat(compte.getId()).isNotNull();
        assertThat(compte.getSolde()).isEqualTo(1500.50);
        assertThat(compte.getType()).isEqualTo(TypeCompte.EPARGNE);
        assertThat(compte.getDateCreation()).isNotNull();
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void getComptesRetourneUneCollectionEnveloppee() {
        service.createCompte(100.0, TypeCompte.COURANT);
        service.createCompte(200.0, TypeCompte.EPARGNE);

        ComptesResponse reponse = service.getComptes();

        assertThat(reponse.getComptes()).hasSize(2);
        assertThat(reponse.getComptes())
                .extracting(Compte::getType)
                .containsExactlyInAnyOrder(TypeCompte.COURANT, TypeCompte.EPARGNE);
    }

    @Test
    void getComptesRetourneUneListeVideSansDonnees() {
        assertThat(service.getComptes().getComptes()).isEmpty();
    }

    @Test
    void getCompteByIdRetourneLeCompteAttendu() throws Exception {
        Compte cree = service.createCompte(3000.0, TypeCompte.COURANT);

        Compte lu = service.getCompteById(cree.getId());

        assertThat(lu.getId()).isEqualTo(cree.getId());
        assertThat(lu.getSolde()).isEqualTo(3000.0);
    }

    @Test
    void getCompteByIdLeveUneFauteSiIdentifiantInconnu() {
        CompteIntrouvableException erreur =
                assertThrows(CompteIntrouvableException.class, () -> service.getCompteById(999L));

        assertThat(erreur.getMessage()).contains("999");
        assertThat(erreur.getFaultInfo()).isEqualTo("identifiant=999");
    }

    @Test
    void getCompteByIdLeveUneFauteSiIdentifiantNull() {
        assertThrows(CompteIntrouvableException.class, () -> service.getCompteById(null));
    }

    @Test
    void deleteCompteSupprimeUnCompteExistant() {
        Compte cree = service.createCompte(50.0, TypeCompte.COURANT);

        boolean supprime = service.deleteCompte(cree.getId());

        assertThat(supprime).isTrue();
        assertThat(repository.existsById(cree.getId())).isFalse();
    }

    @Test
    void deleteCompteRetourneFauxSiDejaSupprime() {
        Compte cree = service.createCompte(50.0, TypeCompte.COURANT);
        service.deleteCompte(cree.getId());

        assertThat(service.deleteCompte(cree.getId())).isFalse();
    }

    @Test
    void deleteCompteRetourneFauxSiIdentifiantNull() {
        assertThat(service.deleteCompte(null)).isFalse();
    }

    @Test
    void deleteCompteSupprimeAussiSesMouvements() throws Exception {
        Compte source = service.createCompte(500.0, TypeCompte.COURANT);
        Compte destination = service.createCompte(100.0, TypeCompte.EPARGNE);
        virementService.virer(source.getId(), destination.getId(), 75.0, "Test suppression");

        boolean supprime = service.deleteCompte(source.getId());

        assertThat(supprime).isTrue();
        assertThat(repository.existsById(source.getId())).isFalse();
        assertThat(mouvementRepository.findByCompteIdOrderByDateOperationAsc(source.getId())).isEmpty();
        assertThat(mouvementRepository.findByCompteIdOrderByDateOperationAsc(destination.getId())).hasSize(1);
    }

    @Test
    void createCompteRefuseUnTypeAbsent() {
        assertThrows(IllegalArgumentException.class, () -> service.createCompte(10.0, null));
    }

    @Test
    void lesComptesSontIsoleesEntreDeuxCreations() {
        service.createCompte(1.0, TypeCompte.COURANT);
        service.createCompte(2.0, TypeCompte.COURANT);

        List<Compte> comptes = service.getComptes().getComptes();

        assertThat(comptes).hasSize(2);
        assertThat(comptes).extracting(Compte::getSolde).containsExactlyInAnyOrder(1.0, 2.0);
    }
}
