package com.example.demo.exception;

import jakarta.xml.ws.WebFault;

/**
 * Faute SOAP explicite : un identifiant inconnu doit produire un Fault
 * identifiable par le client, et non une reponse vide indiscernable.
 */
@WebFault(name = "CompteIntrouvable", targetNamespace = "http://ws.demo.example.com/")
public class CompteIntrouvableException extends Exception {

    private final Long identifiant;

    public CompteIntrouvableException(Long identifiant) {
        super("Aucun compte ne correspond a l'identifiant " + identifiant);
        this.identifiant = identifiant;
    }

    public String getFaultInfo() {
        return "identifiant=" + identifiant;
    }
}
