package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.entities.Compte;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Enveloppe explicite pour la liste des comptes.
 *
 * Sans elle, JAX-WS aplatit les elements d'une List : la reponse contient un
 * seul <comptes> reprenant les champs du premier element, au lieu d'une
 * collection identifiable. Cette enveloppe rend le contrat lisible et stable.
 */
@XmlRootElement(name = "comptes")
@XmlAccessorType(XmlAccessType.FIELD)
public class ComptesResponse {

    @XmlElement(name = "compte")
    private List<Compte> comptes = new ArrayList<>();

    public ComptesResponse() {
    }

    public ComptesResponse(List<Compte> comptes) {
        this.comptes = comptes == null ? new ArrayList<>() : new ArrayList<>(comptes);
    }

    public List<Compte> getComptes() {
        return comptes;
    }

    public void setComptes(List<Compte> comptes) {
        this.comptes = comptes;
    }
}
