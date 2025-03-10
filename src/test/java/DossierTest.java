import org.hdn.api.APIController;
import org.hdn.api.APIResponse;
import org.hdn.api.object.Dossier;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DossierTest {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    void createDossier() {
        try {
            APIController.getInstance().getToken();
            Dossier dossier = new Dossier();
            APIResponse APIResponse = dossier.create();
            assertThat(APIResponse.getResponse().statusCode()).isEqualTo(201);
            logger.info("Dossier created with UUID {}", dossier.getResourceUuid());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
