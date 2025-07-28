package general;

import org.hdn.api.APIController;
import org.hdn.api.APIResponse;
import org.hdn.api.object.Dossier;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
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
            APIResponse apiResponse = dossier.setOnBehalfOf(props.getProperty("senderNode")).create();
            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(201);
            logger.info("Dossier created with UUID {}", dossier.getResourceUuid());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
