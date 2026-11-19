package com.example.demo.ws;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entities.Compte;
import com.example.demo.entities.TypeCompte;
import com.example.demo.exception.CompteIntrouvableException;
import com.example.demo.exception.VirementRefuseException;
import com.example.demo.model.ComptesResponse;
import com.example.demo.model.Releve;
import com.example.demo.model.VirementResultat;
import com.example.demo.repositories.CompteRepository;
import com.example.demo.repositories.MouvementRepository;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

@Component
@WebService(serviceName = "BanqueWS", targetNamespace = "http://ws.demo.example.com/")
public class CompteSoapService {

    private final CompteRepository compteRepository;
    private final MouvementRepository mouvementRepository;
    private final VirementService virementService;

    public CompteSoapService(CompteRepository compteRepository,
                             MouvementRepository mouvementRepository,
                             VirementService virementService) {
        this.compteRepository = compteRepository;
        this.mouvementRepository = mouvementRepository;
        this.virementService = virementService;
    }

    @WebMethod(operationName = "getComptes")
    @WebResult(name = "comptes")
    public ComptesResponse getComptes() {
        List<Compte> comptes = compteRepository.findAll();
        return new ComptesResponse(comptes);
    }

    /**
     * Leve une faute SOAP si l'identifiant est inconnu. Renvoyer null rendrait
     * une reponse vide indiscernable d'une erreur cote client.
     */
    @WebMethod(operationName = "getCompteById")
    @WebResult(name = "compte")
    public Compte getCompteById(@WebParam(name = "id") Long id) throws CompteIntrouvableException {
        if (id == null) {
            throw new CompteIntrouvableException(null);
        }
        return compteRepository.findById(id)
                .orElseThrow(() -> new CompteIntrouvableException(id));
    }

    @WebMethod(operationName = "createCompte")
    @WebResult(name = "compte")
    public Compte createCompte(@WebParam(name = "solde") double solde,
                               @WebParam(name = "type") TypeCompte type) {
        if (type == null) {
            throw new IllegalArgumentException("Le type de compte est obligatoire.");
        }
        Compte compte = new Compte(null, solde, new Date(), type);
        return compteRepository.save(compte);
    }

    /**
     * Un compte ayant des mouvements ne peut pas etre supprime sans supprimer
     * ces mouvements : la cle etrangere l'interdit. Les deux suppressions sont
     * faites dans la meme transaction pour rester coherent.
     */
    @WebMethod(operationName = "deleteCompte")
    @WebResult(name = "supprime")
    @Transactional
    public boolean deleteCompte(@WebParam(name = "id") Long id) {
        if (id == null || !compteRepository.existsById(id)) {
            return false;
        }
        mouvementRepository.deleteByCompteId(id);
        compteRepository.deleteById(id);
        return true;
    }

    @WebMethod(operationName = "virer")
    @WebResult(name = "virement")
    public VirementResultat virer(@WebParam(name = "sourceId") Long sourceId,
                                  @WebParam(name = "destinationId") Long destinationId,
                                  @WebParam(name = "montant") double montant,
                                  @WebParam(name = "libelle") String libelle)
            throws VirementRefuseException {
        return virementService.virer(sourceId, destinationId, montant, libelle);
    }

    @WebMethod(operationName = "releve")
    @WebResult(name = "releve")
    public Releve releve(@WebParam(name = "compteId") Long compteId) throws VirementRefuseException {
        return virementService.releve(compteId);
    }
}