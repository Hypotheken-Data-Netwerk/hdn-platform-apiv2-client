package general;

import org.hdn.api.APIConstants;
import org.hdn.api.APIController;
import org.hdn.api.APIResponse;
import org.hdn.api.object.Dossier;
import org.hdn.api.object.Record;
import org.hdn.api.object.RecordList;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DesktoptaxatieBerichtCalcasaTest {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected static final Properties props = new Properties();

    @BeforeAll
    static void setupBeforeAll() {
        try {
            props.load(new FileInputStream("settings.properties"));
            if (APIController.isNotInitialized())
                APIController.init(props.getProperty("baseURL"), props.getProperty("authURL"), props.getProperty("clientID"), props.getProperty("clientSecret"), props.getProperty("certificate"), props.getProperty("password"));

            APIController.getInstance().getToken();
            new RecordList().setStatus("new").get(props.getProperty("senderNode")).confirmAllRecords(props.getProperty("senderNode"));
            new RecordList().setStatus("read").get(props.getProperty("senderNode")).confirmAllRecords(props.getProperty("senderNode"));
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    void sendDesktoptaxatieBerichtCalcasa() {
        logger.info("Send DesktoptaxatieBericht Calcasa request");
        try {
            APIController.getInstance().getToken();
            Dossier dossier = new Dossier();
            APIResponse apiResponse = dossier.create(props.getProperty("senderNode"));
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
                                            "BronAanvraagBericht",
                                            "24.0",
                                            "AG ABN AMRO Hypotheken Groep",
                                            APIConstants.ContentType.XML,
                                            APIConstants.Environment.production
                                    ),
                                    List.of(new Record.ResponseSchema(
                                            "DesktoptaxatieBericht",
                                            "HDN",
                                            "24.0.1.1",
                                            APIConstants.ContentType.XML
                                    )),
                                    new Record.ExternalSource(
                                            "36 Desktoptaxatie",
                                            "01 Calcasa",
                                            "03 CalCasa"
                                    )
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
                    .setPublicKey(props.getProperty("publickeyUUID")) //"f7e83467-2d36-4d9e-ac34-a27e4810210f")
                    .setMessage("""
                            <?xml version="1.0" encoding="utf-8"?>\r
                            <BronAanvraagBericht>
                            \t<ExterneBron Volgnummer="1">
                            \t\t<SoortProduct>36 Desktoptaxatie</SoortProduct>
                            \t\t<DataleverancierNaam>01 Calcasa</DataleverancierNaam>
                            \t\t<BronNaam>03 CalCasa</BronNaam>
                            \t</ExterneBron>
                            \t<Dataleverancier Volgnummer="1">
                            \t\t<DataMijNr>Test</DataMijNr>
                            \t</Dataleverancier>
                            \t<Product Volgnummer="745">
                            \t\t<Desktoptaxatie Volgnummer="1">
                            \t\t\t<Maatschappij>IN ING Bank</Maatschappij>
                            \t\t\t<Object Volgnummer="1">
                            \t\t\t\t<Postcode>2651NL</Postcode>
                            \t\t\t\t<HuisNr>55</HuisNr>
                            \t\t\t</Object>
                            \t\t</Desktoptaxatie>
                            \t</Product>
                            </BronAanvraagBericht>""")
                    .signMessage();
            apiResponse = apiRecord.create(props.getProperty("senderNode"));
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(201);
            logger.info("Record created with UUID {}", apiRecord.getResourceUuid());

            apiResponse = apiRecord.send(props.getProperty("senderNode"));
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(200);
        } catch (UnrecoverableKeyException | CertificateException | IOException | KeyStoreException |
                 NoSuchAlgorithmException | SignatureException | InvalidKeyException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(2)
    void readDesktoptaxtieBerichtCalcassa() {
        logger.info("Read DesktoptaxatieBericht Calcasa request");
        try {
            APIController.getInstance().getToken();
            List<Record> records = new RecordList().setStatus("new").setMessageType("DesktoptaxatieBericht").setSort("-creationDate").waitForMessage(30, 5000, props.getProperty("senderNode"));
            assertThat(records).isNotNull();

            Record responseRecord = records.getFirst().fetch(props.getProperty("senderNode"));
            assertThat(responseRecord).isNotNull();
            logger.info("Record found with UUID {}", responseRecord.getResourceUuid());

            String message = responseRecord.getMessage();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new java.io.ByteArrayInputStream(message.getBytes()));
            XPath xpath = XPathFactory.newInstance().newXPath();

            assertThat(xpath.compile("DesktoptaxatieBericht/SysteemMelding/MeldingSoort/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("01 Verzoek is met succes verwerkt");

            assertThat(xpath.compile("DesktoptaxatieBericht/Bron/BronNaam/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("03 CalCasa");
            assertThat(xpath.compile("DesktoptaxatieBericht/Bron/SoortProduct/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("36 Desktoptaxatie");

            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[1]/RefObject/text()").evaluate(doc, XPathConstants.STRING)).isNotNull();
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[1]/PeilDt/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("2024-12-19");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[1]/BetrouwbaarheidsNiveau/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("5.4");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[1]/NaamTaxatieKantoor/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("taxatheek");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[1]/NaamTaxateur/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("ING Bank Developers Client");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[1]/OordeelTaxatie/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("01 Goedgekeurd");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[1]/TaxatieDt/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("2024-12-19");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[1]/StatusMelding/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("voltooid");

            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[2]/RefObject/text()").evaluate(doc, XPathConstants.STRING)).isNotNull();
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[2]/PeilDt/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("2024-02-07");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[2]/BetrouwbaarheidsNiveau/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("5.7");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[2]/NaamTaxatieKantoor/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("taxatheek");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[2]/NaamTaxateur/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("Calcasa Default Client");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[2]/OordeelTaxatie/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("01 Goedgekeurd");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[2]/TaxatieDt/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("2024-02-07");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[2]/StatusMelding/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("voltooid");

            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[3]/RefObject/text()").evaluate(doc, XPathConstants.STRING)).isNotNull();
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[3]/PeilDt/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("2024-02-02");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[3]/BetrouwbaarheidsNiveau/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("5.7");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[3]/NaamTaxatieKantoor/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("taxatheek");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[3]/NaamTaxateur/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("Calcasa Default Client");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[3]/OordeelTaxatie/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("01 Goedgekeurd");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[3]/TaxatieDt/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("2024-02-02");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Taxatie)[3]/StatusMelding/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("voltooid");

            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[1]/ObjectSoort/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("10 Portiekflat");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[1]/StraatNaam/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("Koegelwieckplantsoen");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[1]/HuisNr/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("55");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[1]/Postcode/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("2651NL");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[1]/PlaatsNaam/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("Berkel en Rodenrijs");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[1]/MarktWaarde/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("293000.00");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[1]/EnergieKlasse/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("03 A");

            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[2]/ObjectSoort/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("10 Portiekflat");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[2]/StraatNaam/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("Koegelwieckplantsoen");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[2]/HuisNr/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("55");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[2]/Postcode/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("2651NL");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[2]/PlaatsNaam/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("Berkel en Rodenrijs");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[2]/MarktWaarde/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("293000.00");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[2]/EnergieKlasse/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("03 A");

            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[3]/ObjectSoort/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("10 Portiekflat");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[3]/StraatNaam/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("Koegelwieckplantsoen");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[3]/HuisNr/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("55");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[3]/Postcode/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("2651NL");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[3]/PlaatsNaam/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("Berkel en Rodenrijs");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[3]/MarktWaarde/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("293000.00");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/Bron/Object)[3]/EnergieKlasse/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("03 A");

            assertThat(xpath.compile("(//DesktoptaxatieBericht/PrintDoc)[1]/DocSoort/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("100 Dataleverancier oorspronkelijke output");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/PrintDoc)[1]/BestandType/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("JSON JSON");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/PrintDoc)[1]/Encoding/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("B64 Base64-codering");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/PrintDoc)[1]/EncodedData/text()").evaluate(doc, XPathConstants.STRING).toString()).isNotBlank();

            assertThat(xpath.compile("(//DesktoptaxatieBericht/PrintDoc)[2]/DocSoort/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("99 Overig");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/PrintDoc)[2]/BestandType/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("PDF PDF-file");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/PrintDoc)[2]/Encoding/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("B64 Base64-codering");
            assertThat(xpath.compile("(//DesktoptaxatieBericht/PrintDoc)[2]/EncodedData/text()").evaluate(doc, XPathConstants.STRING).toString()).isNotBlank();

            APIResponse apiRResponse = responseRecord.confirm(props.getProperty("senderNode"));
            assertThat(apiRResponse.getResponse().statusCode()).isEqualTo(200);
        } catch (InterruptedException | URISyntaxException | XPathExpressionException | ParserConfigurationException |
                 SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
