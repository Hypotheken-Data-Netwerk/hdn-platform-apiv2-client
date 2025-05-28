import org.hdn.api.APIController;
import org.hdn.api.APIResponse;
import org.hdn.api.object.*;
import org.hdn.api.object.PublicKey;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PublicKeyTest {
    private final String node = APIController.getInstance().getProp("senderNode");
    private final String algorithm = "RSA-SHA256";
    private final String publickey = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUE2a1YxMmFteFVmdFZVZ1FvMm51Nwo3TElZbkxMNG1xQURjZHkyaGppbE85Y0FXbXRHRGxZTnV2dXRkcUt0TFRyWkxkYm5OYUtybldzZzF0WEViSHdRCjdubFB5Qmo4K1oweFFCRWZoVGk1OHVsTlFrM3dPOXEycW5SOWtZOEdjUVhaOFhCS2xXWE5RUFhlTUMrb3J2aHoKUnZOcW9MVVc5WjRJL1d6a0dDcHQ1SFRiWG0zV2thVmovcUd3bjE5blJ2R3V0UlFOK09RaThub3VUekxiVS9XUgpWSVFuVCtCR0ZwemhzR2M2aTJWZyt3eTgraVFMdDBaT1Z3bkFkbWNwK2o3eWpnam9GZGF1d2V1VWNhVU1DK1p0CmY4NmJ1SFFXYzNLdGRsMUtSelJzV2ZyMStkakR2VVBMSVBsQ1hVblFjcjdCU2U4dmhqMkhrN2pQM0t5QWlpZzQKSlFJREFRQUIKLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t";
    private static String resourceUuid;

    @BeforeAll
    static void setupBeforeAll() {
    }

    @Test
    @Order(1)
    void getAlgorithms() {
        try {
            APIController.getInstance().getToken();
            List<String> algorithms = new PublicKeyList().getAlgorithms();
            assertThat(algorithms.size()).isEqualTo(52);
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(2)
    void getPublicKeys() {
        try {
            APIController.getInstance().getToken();
            List<PublicKey> publickeys = new PublicKeyList().get().getPublicKeys();
            assertThat(publickeys.size()).isGreaterThan(1);
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(3)
    void createPublicKey() {
        try {
            APIController.getInstance().getToken();

            PublicKey pk = new PublicKey();
            pk.setAlgorithm(algorithm);
            pk.setPublickey(publickey);
            APIResponse apiResponse = pk.create();
            resourceUuid = pk.getResourceUuid();

            assertThat(apiResponse.getResponse().statusCode()).isEqualTo(201);
            assertThat(resourceUuid).isNotNull();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(4)
    void getPublicKey() {
        try {
            assertThat(resourceUuid).isNotNull();

            APIController.getInstance().getToken();

            PublicKey pk = new PublicKey(resourceUuid);
            pk.fetch();

            assertThat(pk.getAlgorithm()).isEqualTo(algorithm);
            assertThat(pk.getNode()).isEqualTo(node);
            assertThat(pk.getPublickey()).isEqualTo(publickey);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}