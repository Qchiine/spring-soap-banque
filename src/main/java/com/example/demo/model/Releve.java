package com.example.demo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.demo.entities.Mouvement;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Releve d'un compte : solde courant et suite des mouvements enregistres.
 */
@XmlRootElement(name = "releve")
@XmlType(name = "ReleveCompte")
@XmlAccessorType(XmlAccessType.FIELD)
public class Releve {

    private Long compteId;
    private double solde;
    private int nombreMouvements;
    private Date genereLe;

    @XmlElement(name = "mouvement")
    private List<Mouvement> mouvements = new ArrayList<>();

    public Releve() {
    }

    public Releve(Long compteId, double solde, List<Mouvement> mouvements) {
        this.compteId = compteId;
        this.solde = solde;
        this.mouvements = mouvements == null ? new ArrayList<>() : new ArrayList<>(mouvements);
        this.nombreMouvements = this.mouvements.size();
        this.genereLe = new Date();
    }

    public Long getCompteId() {
        return compteId;
    }

    public void setCompteId(Long compteId) {
        this.compteId = compteId;
    }

    public double getSolde() {
        return solde;
    }

    public void setSolde(double solde) {
        this.solde = solde;
    }

    public int getNombreMouvements() {
        return nombreMouvements;
    }

    public void setNombreMouvements(int nombreMouvements) {
        this.nombreMouvements = nombreMouvements;
    }

    public Date getGenereLe() {
        return genereLe;
    }

    public void setGenereLe(Date genereLe) {
        this.genereLe = genereLe;
    }

    public List<Mouvement> getMouvements() {
        return mouvements;
    }

    public void setMouvements(List<Mouvement> mouvements) {
        this.mouvements = mouvements;
    }
}
