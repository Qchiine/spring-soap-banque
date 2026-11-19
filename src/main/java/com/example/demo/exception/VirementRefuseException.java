package com.example.demo.exception;

import jakarta.xml.ws.WebFault;

/**
 * Echec de virement : compte inconnu, montant invalide ou solde insuffisant.
 * Le motif est transmis dans le detail de la faute pour que le client puisse
 * le distinguer d'une erreur technique.
 */
@WebFault(name = "VirementRefuse", targetNamespace = "http://ws.demo.example.com/")
public class VirementRefuseException extends Exception {

    private final String motif;

    public VirementRefuseException(String motif) {
        super(motif);
        this.motif = motif;
    }

    public String getFaultInfo() {
        return "motif=" + motif;
    }

    public String getMotif() {
        return motif;
    }
}
