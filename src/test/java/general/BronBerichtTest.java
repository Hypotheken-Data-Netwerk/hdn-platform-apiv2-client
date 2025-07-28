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
class BronBerichtTest {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected static final Properties props = new Properties();

    @BeforeAll
    static void setupBeforeAll() {
        try {
            props.load(new FileInputStream("settings.properties"));
            if (APIController.isNotInitialized())
                APIController.init(props.getProperty("baseURL"), props.getProperty("authURL"), props.getProperty("clientID"), props.getProperty("clientSecret"), props.getProperty("certificate"), props.getProperty("password"));

            APIController.getInstance().getToken();
            new RecordList().setOnBehalfOf(props.getProperty("senderNode")).setStatus("new").get().confirmAllRecords();
            new RecordList().setOnBehalfOf(props.getProperty("senderNode")).setStatus("read").get().confirmAllRecords();
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    void sendBRPOckto() {
        logger.info("Send BRP Ockto 25 request");
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
                                            "BronAanvraagBericht",
                                            "25.0",
                                            "AG ABN AMRO Hypotheken Groep",
                                            APIConstants.ContentType.XML,
                                            APIConstants.Environment.stage
                                    ),
                                    List.of(new Record.ResponseSchema(
                                            "BasisRegistratiePersonenBericht",
                                            "AG ABN AMRO Hypotheken Groep",
                                            "25.0.1.1",
                                            APIConstants.ContentType.XML
                                    )),
                                    new Record.ExternalSource(
                                            "26 Basis Registratie Personen",
                                            "02 Ockto",
                                            "11 Mijnoverheid.nl"
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
                            <?xml version="1.0" encoding="utf-8"?>
                            <BronAanvraagBericht>
                            \t<ExterneBron Volgnummer="745">
                            \t\t<SoortProduct>26 Basis Registratie Personen</SoortProduct>
                            \t\t<DataleverancierNaam>02 Ockto</DataleverancierNaam>
                            \t\t<BronNaam>11 Mijnoverheid.nl</BronNaam>
                            \t</ExterneBron>
                            \t<Dataleverancier Volgnummer="745">
                            \t\t<DataMijNr>9155605c-1307-476b-b512-b3aa9ac1bb26</DataMijNr>
                            \t\t<DataleverancierID>eyJjb25zdW1lckRhdGEiOnsiRGF0YVNvdXJjZXMiOlsiTU9IIiwiVVdWIiwiVklBIiwiVVBPIiwiRFVPIiwiUkJSIiwiSUtCIiwiSURJTiJdLCJJZCI6IjhiZWU1OTA1LTljMmMtNGEyYS1iZDZjLWIzZjM2YWYzYTNlYyIsIk9kbVZlcnNpb24iOiJWNF80In19</DataleverancierID>
                            \t\t<GegevensVerzoekVersie>ABC-OSN006</GegevensVerzoekVersie>
                            \t\t<DataleverancierOntvangerNaam>ZFL</DataleverancierOntvangerNaam>
                            \t</Dataleverancier>
                            \t<Product Volgnummer="745">
                            \t\t<BasisRegistratiePersonen Volgnummer="745">
                            \t\t\t<PersoonsID>d0228eea-b818-4303-9357-aa043d24b452</PersoonsID>
                            \t\t</BasisRegistratiePersonen>
                            \t</Product>
                            </BronAanvraagBericht>""")
                    .signMessage();
            apiResponse = apiRecord.setOnBehalfOf(props.getProperty("senderNode")).create();
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(201);
            logger.info("Record created with UUID {}", apiRecord.getResourceUuid());

            apiResponse = apiRecord.send();
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(200);
        } catch (UnrecoverableKeyException | CertificateException | IOException | KeyStoreException |
                 NoSuchAlgorithmException | SignatureException | InvalidKeyException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(2)
    void readBRPOckto() {
        logger.info("Read BRP Ockto 25 request");
        try {
            APIController.getInstance().getToken();
            List<Record> records = new RecordList().setStatus("new").setMessageType("ValidatieMelding").setSort("-creationDate").waitForMessage(10, 5000);
            assertThat(records).isNotNull();

            Record responseRecord = records.getFirst().setOnBehalfOf(props.getProperty("senderNode")).fetch();
            assertThat(responseRecord).isNotNull();
            logger.info("Record found with UUID {}", responseRecord.getResourceUuid());

            String message = responseRecord.getMessage();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new java.io.ByteArrayInputStream(message.getBytes()));
            XPath xpath = XPathFactory.newInstance().newXPath();

            assertThat(xpath.compile("ValidatieMelding/SysteemMelding/MeldingSoort/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("99 Specifieke melding");
            assertThat(xpath.compile("ValidatieMelding/SysteemMelding/MeldingSpecifiek/text()").evaluate(doc, XPathConstants.STRING)).isEqualTo("Consent for consumer data has expired");

            APIResponse apiResponse = responseRecord.confirm();
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(200);
        } catch (InterruptedException | URISyntaxException | XPathExpressionException | ParserConfigurationException |
                 SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}