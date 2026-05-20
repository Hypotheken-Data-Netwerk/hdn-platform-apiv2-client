package general;

import nl.hdn.api.APIController;
import nl.hdn.api.APIResponse;
import nl.hdn.api.ConfigUtils;
import nl.hdn.api.object.Dossier;
import nl.hdn.api.object.DossierList;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DossierTest {
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
    void createDossier() {
        try {
            APIController.getInstance().getToken();
            Dossier dossier = new Dossier();
            APIResponse apiResponse = dossier.create(props.getProperty("senderNode"));
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(201);
            logger.info("Dossier created with UUID {}", dossier.getResourceUuid());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void listDossier() {
        try {
            APIController.getInstance().getToken();
            DossierList dossierList = new DossierList();

            APIResponse apiResponse = dossierList.get(props.getProperty("senderNode")).getLastApiResponse();
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(200);

            List<Dossier> dossiers = dossierList.getDossiers();
            assertThat(dossiers).hasSizeGreaterThan(0);
            assertThat(dossiers.getFirst().getSub()).isNotEmpty();
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
