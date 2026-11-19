package com.example.demo.ws;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entities.Compte;
import com.example.demo.entities.Mouvement;
import com.example.demo.exception.VirementRefuseException;
import com.example.demo.model.Releve;
import com.example.demo.model.VirementResultat;
import com.example.demo.repositories.CompteRepository;
import com.example.demo.repositories.MouvementRepository;

/**
 * Virement atomique entre deux comptes.
 *
 * Le debit et le credit sont ecrits dans la meme transaction : si l'une des
 * deux jambes echoue, aucun mouvement partiel n'est conserve. Le solde est
 * arrondi a deux decimales pour eviter les derives de representation flottante.
 */
@Service
public class VirementService {

    private final CompteRepository compteRepository;
    private final MouvementRepository mouvementRepository;

    public VirementService(CompteRepository compteRepository, MouvementRepository mouvementRepository) {
        this.compteRepository = compteRepository;
        this.mouvementRepository = mouvementRepository;
    }

    @Transactional
    public VirementResultat virer(Long sourceId, Long destinationId, double montant, String libelle)
            throws VirementRefuseException {

        if (sourceId == null || destinationId == null) {
            throw new VirementRefuseException("Les deux comptes doivent etre renseignes.");
        }
        if (sourceId.equals(destinationId)) {
            throw new VirementRefuseException("Le compte source et le compte destination doivent differer.");
        }
        if (!(montant > 0)) {
            throw new VirementRefuseException("Le montant doit etre strictement positif.");
        }

        Compte source = compteRepository.findById(sourceId)
                .orElseThrow(() -> new VirementRefuseException("Compte source " + sourceId + " introuvable."));
        Compte destination = compteRepository.findById(destinationId)
                .orElseThrow(() -> new VirementRefuseException("Compte destination " + destinationId + " introuvable."));

        double montantArrondi = arrondir(montant);
        if (source.getSolde() < montantArrondi) {
            throw new VirementRefuseException("Solde insuffisant sur le compte " + sourceId
                    + " : " + source.getSolde() + " disponible, " + montantArrondi + " demande.");
        }

        double soldeSource = arrondir(source.getSolde() - montantArrondi);
        double soldeDestination = arrondir(destination.getSolde() + montantArrondi);

        source.setSolde(soldeSource);
        destination.setSolde(soldeDestination);
        compteRepository.save(source);
        compteRepository.save(destination);

        String motif = (libelle == null || libelle.isBlank()) ? "Virement" : libelle.trim();
        Date maintenant = new Date();

        mouvementRepository.save(new Mouvement(null, source, Mouvement.Sens.DEBIT,
                montantArrondi, soldeSource, maintenant, motif + " vers compte " + destinationId));
        mouvementRepository.save(new Mouvement(null, destination, Mouvement.Sens.CREDIT,
                montantArrondi, soldeDestination, maintenant, motif + " depuis compte " + sourceId));

        return new VirementResultat(sourceId, destinationId, montantArrondi, soldeSource, soldeDestination);
    }

    @Transactional(readOnly = true)
    public Releve releve(Long compteId) throws VirementRefuseException {
        if (compteId == null) {
            throw new VirementRefuseException("L'identifiant du compte est obligatoire.");
        }
        Compte compte = compteRepository.findById(compteId)
                .orElseThrow(() -> new VirementRefuseException("Compte " + compteId + " introuvable."));

        List<Mouvement> mouvements = mouvementRepository.findByCompteIdOrderByDateOperationAsc(compteId);
        return new Releve(compte.getId(), compte.getSolde(), mouvements);
    }

    private double arrondir(double valeur) {
        return BigDecimal.valueOf(valeur).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
