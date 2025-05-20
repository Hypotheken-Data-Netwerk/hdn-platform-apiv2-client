package org.hdn.api;

import java.util.List;

/**
 * Object to hold all constants used in the API
 */
public class APIConstants {
    @SuppressWarnings("unused")
    public static final String RECORD_VALIDATE = "/records/validate";
    public static final String DOSSIERS_GET = "/dossiers";
    public static final String DOSSIER_CREATE = "/dossiers";
    public static final String DOSSIER_GET = "/dossiers/%s";
    public static final String DOSSIER_ADD_NODE = "/dossiers/%s/nodes/add";
    public static final String DOSSIER_GET_RECORDS = "/dossiers/%s/records";
    public static final String DOSSIER_CREATE_RECORD = "/dossiers/%s/records";
    public static final String DOSSIER_CONFIRM_RECORD = "/dossiers/%s/records/%s/confirm";
    public static final String DOSSIER_SEND_RECORD = "/dossiers/%s/records/%s/send";
    public static final String DOSSIER_GET_RECORD = "/dossiers/%s/records/%s";
    public static final String RECORDS_GET = "/records";
    public static final String DOSSIER_GET_EVENTS = "/dossiers/%s/events";
    public static final String RECORD_GET_EVENTS = "/dossiers/%s/records/%s/events";
    public static final String EVENT_GET = "/dossiers/%s/records%s/events/%s";
    public static final String HOOKS_GET = "/hooks";
    public static final String HOOK_CREATE = "/hooks";
    public static final String HOOK_GET = "/hooks/%s";
    public static final String HOOK_DELETE = "/hooks/%s";
    public static final String HOOK_PUT = "/hooks/%s";

    @SuppressWarnings("unused")
    public static final List<String> MessageTypes = List.of(
            "BasisRegistratiePersonenBericht",
            "BronAanvraagBericht",
            "DesktoptaxatieBericht ",
            "DigitaleIdentificatieBericht",
            "EigenaarsinformatieBericht",
            "EigendomsinformatieBericht",
            "EnergieVerbruikBericht",
            "HypotheekinformatieBericht",
            "LoondienstInkomstenBericht",
            "ModelmatigeWaardebepalingBericht",
            "ObjectBericht",
            "OntslagHoofdelijkeAansprakelijkheidBericht",
            "PensioenOverzichtBericht",
            "StudieLeningBericht",
            "TaxatieBericht",
            "ValidatieMelding",
            "VoorafIngevuldeAangifteBericht",
            "AX OfferteAanvraag",
            "CA ConsumentenBronAanvraag",
            "CX ConsumentenBronBericht",
            "DA DocumentAanvraagBericht",
            "DX DocumentBericht",
            "EA ExterneBronAanvraag",
            "EX ExterneBronBericht",
            "IA InformatieAanvraagBericht",
            "IX InformatieBericht",
            "KX KredietAanvraag",
            "LX LevenAanvraag",
            "MX BeheerVerzoek",
            "OX Offerte",
            "SX StatusMelding",
            "WX WaarborgBericht",
            "ZX InkomensverklaringOndernemerAanvraag"
    );

    public enum Environment {
        production,
        stage,
        acceptatie
    }

    public enum ContentType {
        XML
    }
}