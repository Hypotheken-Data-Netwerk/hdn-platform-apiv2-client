import org.hdn.api.APIConstants;
import org.hdn.api.APIController;
import org.hdn.api.APIResponse;
import org.hdn.api.object.Dossier;
import org.hdn.api.object.Record;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RecordTest {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    void createRecord() {
        try {
            APIController.getInstance().getToken();
            Dossier dossier = new Dossier();
            APIResponse apiResponse = dossier.create();
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(201);
            logger.info("Dossier created with UUID {}", dossier.getResourceUuid());

            Record record = new Record(dossier.getResourceUuid())
                    .setHeader(
                            new Record.Header(
                                    "1",
                                    dossier.getResourceUuid(),
                                    APIController.getInstance().getProp("senderNode"),
                                    APIController.getInstance().getProp("receiverNode"),
                                    new Record.RequestSchema(
                                            "AX OfferteAanvraag",
                                            "24.0",
                                            "AG ABN AMRO Hypotheken Groep",
                                            APIConstants.ContentType.XML,
                                            APIConstants.Environment.production
                                    ),
                                    List.of(),
                                    null
                            )
                    )
                    .setMiscellaneous(
                            new Record.Miscellaneous(
                                    "Doniek Advies",
                                    "Doniek Hypotheken",
                                    new Record.SendingApplication(
                                            "HDN Library",
                                            "1.0",
                                            Instant.now()
                                    )
                            )
                    )
                    .setPublicKey(APIController.getInstance().getProp("publickeyUUID"))
                    .setMessage("""
                            <?xml version="1.0" encoding="utf-8"?>\r
                            <OfferteAanvraag xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">\r
                            \t<HuidigObject Volgnummer="1">\r
                            \t\t<ObjectSoort>01 Vrijstaande woning</ObjectSoort>\r
                            \t\t<StraatNaam>Straatnaam</StraatNaam>\r
                            \t\t<HuisNr>2</HuisNr>\r
                            \t\t<Postcode>1234AA</Postcode>\r
                            \t\t<PlaatsNaam>Plaatsnaam</PlaatsNaam>\r
                            \t\t<Land>NL Nederland</Land>\r
                            \t\t<TeVerkopenJN>N Nee</TeVerkopenJN>\r
                            \t\t<VerkochtJN>N Nee</VerkochtJN>\r
                            \t\t<MarktWaarde>300000.00</MarktWaarde>\r
                            \t\t<OverbruggingJN>N Nee</OverbruggingJN>\r
                            \t\t<HuidigeFinancieringJN>J Ja</HuidigeFinancieringJN>\r
                            \t\t<HuidigeFinanciering Volgnummer="1">\r
                            \t\t\t<AflossenJN>J Ja</AflossenJN>\r
                            \t\t\t<FinancierBestaandeHypotheek>ZZ Anders</FinancierBestaandeHypotheek>\r
                            \t\t\t<FinancierOmschr>svn</FinancierOmschr>\r
                            \t\t\t<BestaandeHypotheekSaldo>145000.00</BestaandeHypotheekSaldo>\r
                            \t\t\t<BedragInschrijving>145000.00</BedragInschrijving>\r
                            \t\t\t<BestaandeHypotheekNr>656828</BestaandeHypotheekNr>\r
                            \t\t\t<Eigenwoningschuld>145000.00</Eigenwoningschuld>\r
                            \t\t\t<RangOrde>1</RangOrde>\r
                            \t\t</HuidigeFinanciering>\r
                            \t</HuidigObject>\r
                            \t<Hypotheekgever Volgnummer="1">\r
                            \t\t<RefPartijNAWData IDREF="NA01"/>\r
                            \t\t<TelefoonNrPrive>0612345678</TelefoonNrPrive>\r
                            \t\t<TelefoonNrWerk>0612345678</TelefoonNrWerk>\r
                            \t\t<TelefoonNrMobiel>0612345678</TelefoonNrMobiel>\r
                            \t\t<E-mailadres>test@gmail.com</E-mailadres>\r
                            \t\t<BurgerlijkeStaat>03 Alleenstaand</BurgerlijkeStaat>\r
                            \t\t<HuwelijkOntbondenJN>N Nee</HuwelijkOntbondenJN>\r
                            \t\t<WeduweWeduwnaarJN>N Nee</WeduweWeduwnaarJN>\r
                            \t\t<HoofdelijkAansprakelijkJN>J Ja</HoofdelijkAansprakelijkJN>\r
                            \t\t<AfwijkendKlantProfielJN>N Nee</AfwijkendKlantProfielJN>\r
                            \t\t<FiscaalInwonerVanNederlandJN>J Ja</FiscaalInwonerVanNederlandJN>\r
                            \t\t<UitsluitendOfMedeFiscaalInwonerBuitenNedJN>N Nee</UitsluitendOfMedeFiscaalInwonerBuitenNedJN>\r
                            \t\t<StudieleningInventarisatie>02 Ja, maar geen sprake van een studielening</StudieleningInventarisatie>\r
                            \t\t<Inkomsten Volgnummer="1">\r
                            \t\t\t<Uitkering Volgnummer="1">\r
                            \t\t\t\t<SoortUitkering>09 AOW</SoortUitkering>\r
                            \t\t\t\t<BrutoJaarUitkering>14217.00</BrutoJaarUitkering>\r
                            \t\t\t\t<IngangsDt>2022-12-12</IngangsDt>\r
                            \t\t\t\t<AOWVolledigeOpbouwJN>J Ja</AOWVolledigeOpbouwJN>\r
                            \t\t\t</Uitkering>\r
                            \t\t</Inkomsten>\r
                            \t\t<Identificatie>\r
                            \t\t\t<LegitimatieSoort>PP Paspoort</LegitimatieSoort>\r
                            \t\t\t<LegitimatieNr>NS1234K1</LegitimatieNr>\r
                            \t\t</Identificatie>\r
                            \t</Hypotheekgever>\r
                            \t<PartijNAWData Volgnummer="1" ID="NA01">\r
                            \t\t<SoortPartij>01 natuurlijk persoon</SoortPartij>\r
                            \t\t<VoorNamen>Voornaam</VoorNamen>\r
                            \t\t<GeboorteAchternaam>Achternaam25</GeboorteAchternaam>\r
                            \t\t<CorrespondentieNaam>Achternaam</CorrespondentieNaam>\r
                            \t\t<Nationaliteit>NL Nederland</Nationaliteit>\r
                            \t\t<GeboortePlaats>Geboorteplaats</GeboortePlaats>\r
                            \t\t<GeboorteLand>NL Nederland</GeboorteLand>\r
                            \t\t<VoorLetters>V</VoorLetters>\r
                            \t\t<GeboorteDt>1958-12-12</GeboorteDt>\r
                            \t\t<StraatNaam>Straat</StraatNaam>\r
                            \t\t<HuisNr>1</HuisNr>\r
                            \t\t<Postcode>2222BB</Postcode>\r
                            \t\t<PlaatsNaam>Plaats</PlaatsNaam>\r
                            \t\t<Land>NL Nederland</Land>\r
                            \t\t<RekeningNr>NL01INGB0000000000</RekeningNr>\r
                            \t</PartijNAWData>\r
                            \t<PartijNAWData Volgnummer="2" ID="NA03">\r
                            \t\t<SoortPartij>02 rechtspersoon</SoortPartij>\r
                            \t\t<VoorNamen>Rechtspersoonnaam</VoorNamen>\r
                            \t\t<CorrespondentieNaam>Voornaam</CorrespondentieNaam>\r
                            \t\t<CorrespondentieTussenvoegsels>van</CorrespondentieTussenvoegsels>\r
                            \t\t<GeboortePlaats>Testplaats</GeboortePlaats>\r
                            \t\t<VoorLetters>R</VoorLetters>\r
                            \t\t<StraatNaam>Straatnaam</StraatNaam>\r
                            \t\t<HuisNr>23</HuisNr>\r
                            \t\t<Postcode>3333CC</Postcode>\r
                            \t\t<PlaatsNaam>Testplaats</PlaatsNaam>\r
                            \t\t<Land>NL Nederland</Land>\r
                            \t</PartijNAWData>\r
                            \t<TussenPersoon Volgnummer="1">\r
                            \t\t<RefContactPersoonData IDREF="NA03"/>\r
                            \t\t<BedrijfsNaam>Testbedrijf</BedrijfsNaam>\r
                            \t\t<TussenpersoonNr>1</TussenpersoonNr>\r
                            \t\t<TelefoonNrWerk>0612121212</TelefoonNrWerk>\r
                            \t\t<E-mailadres>tester@gmailcom</E-mailadres>\r
                            \t\t<TelefoonNrMobiel>0612121212</TelefoonNrMobiel>\r
                            \t\t<AFMRegistratieNr>22889966</AFMRegistratieNr>\r
                            \t\t<InitieelPakket>Testpakket</InitieelPakket>\r
                            \t</TussenPersoon>\r
                            \t<Object Volgnummer="1">\r
                            \t\t<ObjectSoort>06 Tussenwoning</ObjectSoort>\r
                            \t\t<GarageJN>N Nee</GarageJN>\r
                            \t\t<RecreatieveBewoningJN>N Nee</RecreatieveBewoningJN>\r
                            \t\t<Onderpand>01 bestaand</Onderpand>\r
                            \t\t<StraatNaam>Straatnaam</StraatNaam>\r
                            \t\t<HuisNr>2</HuisNr>\r
                            \t\t<Postcode>8888ZZ</Postcode>\r
                            \t\t<PlaatsNaam>Testplaats</PlaatsNaam>\r
                            \t\t<Land>NL Nederland</Land>\r
                            \t\t<EigenBewoningJN>J Ja</EigenBewoningJN>\r
                            \t\t<ErfpachtJN>N Nee</ErfpachtJN>\r
                            \t\t<BouwJaar>1991</BouwJaar>\r
                            \t\t<AppartementsrechtJN>N Nee</AppartementsrechtJN>\r
                            \t\t<TaxatieDt>2024-06-02</TaxatieDt>\r
                            \t\t<KostenTaxatie>350.00</KostenTaxatie>\r
                            \t\t<MarktWaarde>300000.00</MarktWaarde>\r
                            \t\t<Koopsom>0.00</Koopsom>\r
                            \t\t<WOZWaarde>300000.00</WOZWaarde>\r
                            \t\t<VerduurzamingBesprokenJN>J Ja</VerduurzamingBesprokenJN>\r
                            \t\t<VerduurzamingToepassen>01 Nee</VerduurzamingToepassen>\r
                            \t\t<BedragKwaliteitsverbeteringEBV>0.00</BedragKwaliteitsverbeteringEBV>\r
                            \t\t<OpleveringsDt>2024-05-12</OpleveringsDt>\r
                            \t</Object>\r
                            \t<Lening Volgnummer="1">\r
                            \t\t<HypotheekBedrag>145000.00</HypotheekBedrag>\r
                            \t\t<HypothecaireInschrijving>145000.00</HypothecaireInschrijving>\r
                            \t\t<Financier>AE AEGON Verzekeringen</Financier>\r
                            \t\t<Regeling>08 oversluiting andere geldgever</Regeling>\r
                            \t\t<RangOrde>1</RangOrde>\r
                            \t\t<CodeLeningMij>AE011 Aegon Hypotheek</CodeLeningMij>\r
                            \t\t<PasseerDt>2024-06-12</PasseerDt>\r
                            \t\t<IngebrachtEigenMiddelen>8408.00</IngebrachtEigenMiddelen>\r
                            \t\t<NettoAdvieskosten>1500.00</NettoAdvieskosten>\r
                            \t\t<BemiddelingsKosten>0.00</BemiddelingsKosten>\r
                            \t\t<BoeteRente>6200.00</BoeteRente>\r
                            \t\t<NHGJN>N Nee</NHGJN>\r
                            \t\t<Mutatie>32 Oversluiten zonder verhoging</Mutatie>\r
                            \t\t<HypotheekAkteKostenSpec>708.00</HypotheekAkteKostenSpec>\r
                            \t\t<AankoopWoningJN>N Nee</AankoopWoningJN>\r
                            \t\t<Leningdeel Volgnummer="1">\r
                            \t\t\t<CodeLeningDeel>1 Nieuw</CodeLeningDeel>\r
                            \t\t\t<BelastingBox>03 Box 3</BelastingBox>\r
                            \t\t\t<CodeDeelMij>AE002 Aegon Annuïteitenhypotheek</CodeDeelMij>\r
                            \t\t\t<VasteEindDtLeningdeelJN>N Nee</VasteEindDtLeningdeelJN>\r
                            \t\t\t<DuurInMnd>360</DuurInMnd>\r
                            \t\t\t<LeningDeelBedrag>145000.00</LeningDeelBedrag>\r
                            \t\t\t<AflossingsVorm>01 Annuiteit</AflossingsVorm>\r
                            \t\t\t<RenteAfspraak>01 rentevast</RenteAfspraak>\r
                            \t\t\t<RenteVastInMnd>120</RenteVastInMnd>\r
                            \t\t\t<RenteBedenkTijd>01 geen</RenteBedenkTijd>\r
                            \t\t\t<BetalingsTermijn>01 per maand</BetalingsTermijn>\r
                            \t\t\t<BetalingAchterafJN>J Ja</BetalingAchterafJN>\r
                            \t\t\t<RentePct>2.40</RentePct>\r
                            \t\t\t<RenteAfspraakOmschr>Doe</RenteAfspraakOmschr>\r
                            \t\t\t<RentePctBovenMarge>0.00</RentePctBovenMarge>\r
                            \t\t\t<RentePctOnderMarge>0.00</RentePctOnderMarge>\r
                            \t\t</Leningdeel>\r
                            \t</Lening>\r
                            </OfferteAanvraag>""")
                    .signMessage();
            apiResponse = record.create();
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(201);
            logger.info("Record created with UUID {}", record.getResourceUuid());

            apiResponse = record.send();
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(200);
        } catch (IOException | InterruptedException | UnrecoverableKeyException | CertificateException |
                 KeyStoreException | NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
