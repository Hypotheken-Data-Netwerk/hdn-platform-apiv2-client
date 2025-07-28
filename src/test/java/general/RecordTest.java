package general;

import org.hdn.api.APIConstants;
import org.hdn.api.APIController;
import org.hdn.api.APIResponse;
import org.hdn.api.object.Dossier;
import org.hdn.api.object.Record;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecordTest {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected static final Properties props = new Properties();

    @BeforeAll
    static void setupBeforeAll() {
        try {
            props.load(new FileInputStream("settings.properties"));
            if (APIController.isNotInitialized())
                APIController.init(props.getProperty("baseURL"), props.getProperty("authURL"), props.getProperty("clientID"), props.getProperty("clientSecret"), props.getProperty("certificate"), props.getProperty("password"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createRecord() {
        try {
            APIController.getInstance().getToken();
            Dossier dossier = new Dossier();
            APIResponse apiResponse = dossier.setOnBehalfOf(props.getProperty("senderNode")).create();
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(201);
            logger.info("Dossier created with UUID {}", dossier.getResourceUuid());

            Record apiRecord = new Record(dossier.getResourceUuid())
                    .setHeader(
                            new Record.Header(
                                    "1",
                                    dossier.getResourceUuid(),
                                    props.getProperty("senderNode"),
                                    props.getProperty("receiverNode"),
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
                    .setPublicKey(props.getProperty("publickeyUUID"))
                    .setMessage("""
                            <?xml version="1.0" encoding="utf-8"?>
                            <OfferteAanvraag xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
                            	<HuidigObject Volgnummer="1">
                            		<ObjectSoort>01 Vrijstaande woning</ObjectSoort>
                            		<StraatNaam>Straatnaam</StraatNaam>
                            		<HuisNr>2</HuisNr>
                            		<Postcode>1234AA</Postcode>
                            		<PlaatsNaam>Plaatsnaam</PlaatsNaam>
                            		<Land>NL Nederland</Land>
                            		<TeVerkopenJN>N Nee</TeVerkopenJN>
                            		<VerkochtJN>N Nee</VerkochtJN>
                            		<MarktWaarde>300000.00</MarktWaarde>
                            		<OverbruggingJN>N Nee</OverbruggingJN>
                            		<HuidigeFinancieringJN>J Ja</HuidigeFinancieringJN>
                            		<HuidigeFinanciering Volgnummer="1">
                            			<AflossenJN>J Ja</AflossenJN>
                            			<FinancierBestaandeHypotheek>ZZ Anders</FinancierBestaandeHypotheek>
                            			<FinancierOmschr>svn</FinancierOmschr>
                            			<BestaandeHypotheekSaldo>145000.00</BestaandeHypotheekSaldo>
                            			<BedragInschrijving>145000.00</BedragInschrijving>
                            			<BestaandeHypotheekNr>656828</BestaandeHypotheekNr>
                            			<Eigenwoningschuld>145000.00</Eigenwoningschuld>
                            			<RangOrde>1</RangOrde>
                            		</HuidigeFinanciering>
                            	</HuidigObject>
                            	<Hypotheekgever Volgnummer="1">
                            		<RefPartijNAWData IDREF="NA01"/>
                            		<TelefoonNrPrive>0612345678</TelefoonNrPrive>
                            		<TelefoonNrWerk>0612345678</TelefoonNrWerk>
                            		<TelefoonNrMobiel>0612345678</TelefoonNrMobiel>
                            		<E-mailadres>test@gmail.com</E-mailadres>
                            		<BurgerlijkeStaat>03 Alleenstaand</BurgerlijkeStaat>
                            		<HuwelijkOntbondenJN>N Nee</HuwelijkOntbondenJN>
                            		<WeduweWeduwnaarJN>N Nee</WeduweWeduwnaarJN>
                            		<HoofdelijkAansprakelijkJN>J Ja</HoofdelijkAansprakelijkJN>
                            		<AfwijkendKlantProfielJN>N Nee</AfwijkendKlantProfielJN>
                            		<FiscaalInwonerVanNederlandJN>J Ja</FiscaalInwonerVanNederlandJN>
                            		<UitsluitendOfMedeFiscaalInwonerBuitenNedJN>N Nee</UitsluitendOfMedeFiscaalInwonerBuitenNedJN>
                            		<StudieleningInventarisatie>02 Ja, maar geen sprake van een studielening</StudieleningInventarisatie>
                            		<Inkomsten Volgnummer="1">
                            			<Uitkering Volgnummer="1">
                            				<SoortUitkering>09 AOW</SoortUitkering>
                            				<BrutoJaarUitkering>14217.00</BrutoJaarUitkering>
                            				<IngangsDt>2022-12-12</IngangsDt>
                            				<AOWVolledigeOpbouwJN>J Ja</AOWVolledigeOpbouwJN>
                            			</Uitkering>
                            		</Inkomsten>
                            		<Identificatie>
                            			<LegitimatieSoort>PP Paspoort</LegitimatieSoort>
                            			<LegitimatieNr>NS1234K1</LegitimatieNr>
                            		</Identificatie>
                            	</Hypotheekgever>
                            	<PartijNAWData Volgnummer="1" ID="NA01">
                            		<SoortPartij>01 natuurlijk persoon</SoortPartij>
                            		<VoorNamen>Voornaam</VoorNamen>
                            		<GeboorteAchternaam>Achternaam</GeboorteAchternaam>
                            		<CorrespondentieNaam>Achternaam</CorrespondentieNaam>
                            		<Nationaliteit>NL Nederland</Nationaliteit>
                            		<GeboortePlaats>Geboorteplaats</GeboortePlaats>
                            		<GeboorteLand>NL Nederland</GeboorteLand>
                            		<VoorLetters>V</VoorLetters>
                            		<GeboorteDt>1958-12-12</GeboorteDt>
                            		<StraatNaam>Straat</StraatNaam>
                            		<HuisNr>1</HuisNr>
                            		<Postcode>2222BB</Postcode>
                            		<PlaatsNaam>Plaats</PlaatsNaam>
                            		<Land>NL Nederland</Land>
                            		<RekeningNr>NL01INGB0000000000</RekeningNr>
                            	</PartijNAWData>
                            	<PartijNAWData Volgnummer="2" ID="NA03">
                            		<SoortPartij>02 rechtspersoon</SoortPartij>
                            		<VoorNamen>Rechtspersoonnaam</VoorNamen>
                            		<CorrespondentieNaam>Voornaam</CorrespondentieNaam>
                            		<CorrespondentieTussenvoegsels>van</CorrespondentieTussenvoegsels>
                            		<GeboortePlaats>Testplaats</GeboortePlaats>
                            		<VoorLetters>R</VoorLetters>
                            		<StraatNaam>Straatnaam</StraatNaam>
                            		<HuisNr>23</HuisNr>
                            		<Postcode>3333CC</Postcode>
                            		<PlaatsNaam>Testplaats</PlaatsNaam>
                            		<Land>NL Nederland</Land>
                            	</PartijNAWData>
                            	<TussenPersoon Volgnummer="1">
                            		<RefContactPersoonData IDREF="NA03"/>
                            		<BedrijfsNaam>Testbedrijf</BedrijfsNaam>
                            		<TussenpersoonNr>1</TussenpersoonNr>
                            		<TelefoonNrWerk>0612121212</TelefoonNrWerk>
                            		<E-mailadres>tester@gmailcom</E-mailadres>
                            		<TelefoonNrMobiel>0612121212</TelefoonNrMobiel>
                            		<AFMRegistratieNr>22889966</AFMRegistratieNr>
                            		<InitieelPakket>Testpakket</InitieelPakket>
                            	</TussenPersoon>
                            	<Object Volgnummer="1">
                            		<ObjectSoort>06 Tussenwoning</ObjectSoort>
                            		<GarageJN>N Nee</GarageJN>
                            		<RecreatieveBewoningJN>N Nee</RecreatieveBewoningJN>
                            		<Onderpand>01 bestaand</Onderpand>
                            		<StraatNaam>Straatnaam</StraatNaam>
                            		<HuisNr>2</HuisNr>
                            		<Postcode>8888ZZ</Postcode>
                            		<PlaatsNaam>Testplaats</PlaatsNaam>
                            		<Land>NL Nederland</Land>
                            		<EigenBewoningJN>J Ja</EigenBewoningJN>
                            		<ErfpachtJN>N Nee</ErfpachtJN>
                            		<BouwJaar>1991</BouwJaar>
                            		<AppartementsrechtJN>N Nee</AppartementsrechtJN>
                            		<TaxatieDt>2024-06-02</TaxatieDt>
                            		<KostenTaxatie>350.00</KostenTaxatie>
                            		<MarktWaarde>300000.00</MarktWaarde>
                            		<Koopsom>0.00</Koopsom>
                            		<WOZWaarde>300000.00</WOZWaarde>
                            		<VerduurzamingBesprokenJN>J Ja</VerduurzamingBesprokenJN>
                            		<VerduurzamingToepassen>01 Nee</VerduurzamingToepassen>
                            		<BedragKwaliteitsverbeteringEBV>0.00</BedragKwaliteitsverbeteringEBV>
                            		<OpleveringsDt>2024-05-12</OpleveringsDt>
                            	</Object>
                            	<Lening Volgnummer="1">
                            		<HypotheekBedrag>145000.00</HypotheekBedrag>
                            		<HypothecaireInschrijving>145000.00</HypothecaireInschrijving>
                            		<Financier>AE AEGON Verzekeringen</Financier>
                            		<Regeling>08 oversluiting andere geldgever</Regeling>
                            		<RangOrde>1</RangOrde>
                            		<CodeLeningMij>AE011 Aegon Hypotheek</CodeLeningMij>
                            		<PasseerDt>2024-06-12</PasseerDt>
                            		<IngebrachtEigenMiddelen>8408.00</IngebrachtEigenMiddelen>
                            		<NettoAdvieskosten>1500.00</NettoAdvieskosten>
                            		<BemiddelingsKosten>0.00</BemiddelingsKosten>
                            		<BoeteRente>6200.00</BoeteRente>
                            		<NHGJN>N Nee</NHGJN>
                            		<Mutatie>32 Oversluiten zonder verhoging</Mutatie>
                            		<HypotheekAkteKostenSpec>708.00</HypotheekAkteKostenSpec>
                            		<AankoopWoningJN>N Nee</AankoopWoningJN>
                            		<Leningdeel Volgnummer="1">
                            			<CodeLeningDeel>1 Nieuw</CodeLeningDeel>
                            			<BelastingBox>03 Box 3</BelastingBox>
                            			<CodeDeelMij>AE002 Aegon Annuïteitenhypotheek</CodeDeelMij>
                            			<VasteEindDtLeningdeelJN>N Nee</VasteEindDtLeningdeelJN>
                            			<DuurInMnd>360</DuurInMnd>
                            			<LeningDeelBedrag>145000.00</LeningDeelBedrag>
                            			<AflossingsVorm>01 Annuiteit</AflossingsVorm>
                            			<RenteAfspraak>01 rentevast</RenteAfspraak>
                            			<RenteVastInMnd>120</RenteVastInMnd>
                            			<RenteBedenkTijd>01 geen</RenteBedenkTijd>
                            			<BetalingsTermijn>01 per maand</BetalingsTermijn>
                            			<BetalingAchterafJN>J Ja</BetalingAchterafJN>
                            			<RentePct>2.40</RentePct>
                            			<RenteAfspraakOmschr>Doe</RenteAfspraakOmschr>
                            			<RentePctBovenMarge>0.00</RentePctBovenMarge>
                            			<RentePctOnderMarge>0.00</RentePctOnderMarge>
                            		</Leningdeel>
                            	</Lening>
                            </OfferteAanvraag>""")
                    .signMessage();
            apiResponse = apiRecord.setOnBehalfOf(props.getProperty("senderNode")).create();
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(201);
            logger.info("Record created with UUID {}", apiRecord.getResourceUuid());

            apiResponse = apiRecord.send();
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(200);
        } catch (IOException | InterruptedException | UnrecoverableKeyException | CertificateException |
                 KeyStoreException | NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
