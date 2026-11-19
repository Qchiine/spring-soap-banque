package com.example.demo.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Resultat d'un virement : identifiants des deux comptes, montant deplace
 * et soldes apres operation. Permet au client de verifier l'equilibre.
 */
@XmlRootElement(name = "virement")
@XmlAccessorType(XmlAccessType.FIELD)
public class VirementResultat {

    private Long compteSourceId;
    private Long compteDestinationId;
    private double montant;
    private double soldeSourceApres;
    private double soldeDestinationApres;

    public VirementResultat() {
    }

    public VirementResultat(Long compteSourceId, Long compteDestinationId, double montant,
                            double soldeSourceApres, double soldeDestinationApres) {
        this.compteSourceId = compteSourceId;
        this.compteDestinationId = compteDestinationId;
        this.montant = montant;
        this.soldeSourceApres = soldeSourceApres;
        this.soldeDestinationApres = soldeDestinationApres;
    }

    public Long getCompteSourceId() {
        return compteSourceId;
    }

    public void setCompteSourceId(Long compteSourceId) {
        this.compteSourceId = compteSourceId;
    }

    public Long getCompteDestinationId() {
        return compteDestinationId;
    }

    public void setCompteDestinationId(Long compteDestinationId) {
        this.compteDestinationId = compteDestinationId;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public double getSoldeSourceApres() {
        return soldeSourceApres;
    }

    public void setSoldeSourceApres(double soldeSourceApres) {
        this.soldeSourceApres = soldeSourceApres;
    }

    public double getSoldeDestinationApres() {
        return soldeDestinationApres;
    }

    public void setSoldeDestinationApres(double soldeDestinationApres) {
        this.soldeDestinationApres = soldeDestinationApres;
    }
}
