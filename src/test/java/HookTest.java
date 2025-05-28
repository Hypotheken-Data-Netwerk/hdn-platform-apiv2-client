import org.hdn.api.APIConstants;
import org.hdn.api.APIController;
import org.hdn.api.APIResponse;
import org.hdn.api.object.Dossier;
import org.hdn.api.object.Hook;
import org.hdn.api.object.HookList;
import org.hdn.api.object.Record;
import org.hdn.api.object.RecordList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HookTest {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @BeforeAll
    static void setupBeforeAll() {
        try {
            APIController.getInstance().getToken();
            new RecordList().setStatus("new").get().confirmAllRecords();
            new RecordList().setStatus("read").get().confirmAllRecords();
            new HookList().get().getHooks().forEach(hook -> {
                try {
                    hook.delete();
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    void createHook() {
        APIController.getInstance().getToken();
        Hook hook = new Hook();
        try {
            APIResponse apiResponse = hook.setUrl("https://hdna.redbluetechnologies.com")
                    .setNodes(new String[]{APIController.getInstance().getProp("senderNode")})
                    .setAuthenticationMethod("none")
                    .create();
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(201);
            logger.info("Hook created with resource uuid {}", hook.getResourceUuid());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(2)
    void getAllHooks() {
        APIController.getInstance().getToken();
        try {
            List<Hook> hooks = new HookList().get().getHooks();
            assertThat(hooks.size()).isEqualTo(1);
            assertThat(hooks.getFirst().getUrl()).isEqualTo("https://hdna.redbluetechnologies.com");
            assertThat(hooks.getFirst().getNodes()).isEqualTo(new String[]{APIController.getInstance().getProp("senderNode")});
            assertThat(hooks.getFirst().getAuthenticationMethod()).isEqualTo("none");
            assertThat(hooks.getFirst().getMessageTypes()).isEqualTo(new String[0]);
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(3)
    void updateHook() {
        try {
            Hook hook = new HookList().get().getHooks().getFirst();
            APIResponse apiResponse = hook.setUrl("https://hdn.redbluetechnologies.com").update();
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(200);
            assertThat(hook.getUrl()).isEqualTo("https://hdn.redbluetechnologies.com");
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(4)
    void callHook() {
        logger.info("Send invalid message");
        try {
            APIController.getInstance().getToken();
            Dossier dossier = new Dossier();
            APIResponse APIResponse = dossier.create();
            assertThat(APIResponse.getResponse().statusCode()).isEqualTo(201);
            logger.info("Dossier created with UUID {}", dossier.getResourceUuid());

            org.hdn.api.object.Record record = new org.hdn.api.object.Record(dossier.getResourceUuid())
                    .setHeader(
                            new org.hdn.api.object.Record.Header(
                                    "1",
                                    dossier.getResourceUuid(),
                                    APIController.getInstance().getProp("senderNode"),
                                    APIController.getInstance().getProp("receiverNode"),
                                    new org.hdn.api.object.Record.RequestSchema(
                                            "BronAanvraagBericht",
                                            "25.0",
                                            "AG ABN AMRO Hypotheken Groep",
                                            APIConstants.ContentType.XML,
                                            APIConstants.Environment.stage
                                    ),
                                    List.of(new org.hdn.api.object.Record.ResponseSchema(
                                            "BasisRegistratiePersonenBericht",
                                            "AG ABN AMRO Hypotheken Groep",
                                            "25.0.1.1",
                                            APIConstants.ContentType.XML
                                    )),
                                    new org.hdn.api.object.Record.ExternalSource(
                                            "26 Basis Registratie Personen",
                                            "02 Ockto",
                                            "11 Mijnoverheid.nl"
                                    )
                            )
                    )
                    .setMiscellaneous(
                            new org.hdn.api.object.Record.Miscellaneous(
                                    "Doniek Advies",
                                    "Doniek Hypotheken",
                                    new org.hdn.api.object.Record.SendingApplication(
                                            "HDN Library",
                                            "1.0",
                                            Instant.now()
                                    )
                            )
                    )
                    .setPublicKey(APIController.getInstance().getProp("publickeyUUID")) //"f7e83467-2d36-4d9e-ac34-a27e4810210f")
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
            APIResponse = record.create();
            assertThat(APIResponse.getResponse().statusCode()).isEqualTo(201);
            logger.info("Record created with UUID {}", record.getResourceUuid());

            APIResponse = record.send();
            assertThat(APIResponse.getResponse().statusCode()).isEqualTo(200);

            List<org.hdn.api.object.Record> records = new RecordList().setStatus("new").setMessageType("ValidatieMelding").setSort("-creationDate").waitForMessage(10, 5000);
            assertThat(records).isNotNull();

            Record responseRecord = records.getFirst().fetch();
            assertThat(responseRecord).isNotNull();
            logger.info("Record found with resource UUID {} and dossier UUID {}", responseRecord.getResourceUuid(), responseRecord.getDossierUuid());

            URI uri = new URI("https", "hdn.redbluetechnologies.com", "/lookup.php", "dossierUuid="+responseRecord.getDossierUuid()+"&recordUuid="+responseRecord.getResourceUuid(), null);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONArray hookRecords = new JSONObject(response.toString()).getJSONArray("data");
            assertThat(hookRecords.length()).isEqualTo(1);
            JSONObject hookData = new JSONObject(((JSONObject) hookRecords.get(0)).getString("request"));
            assertThat(hookData.getString("notification_recipient")).isEqualTo(APIController.getInstance().getProp("senderNode"));
            assertThat(hookData.getJSONObject("link").getString("href")).contains("/"+responseRecord.getDossierUuid()+"/records/"+responseRecord.getResourceUuid());

            APIResponse = responseRecord.confirm();
            assertThat(APIResponse.getResponse().statusCode()).isEqualTo(200);
        } catch (UnrecoverableKeyException | CertificateException | IOException | KeyStoreException |
                 NoSuchAlgorithmException | SignatureException | InvalidKeyException | InterruptedException |
                 URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(5)
    void deleteHook() {
        try {
            Hook hook = new HookList().get().getHooks().getFirst();
            APIResponse apiResponse = hook.delete();
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(204);
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
