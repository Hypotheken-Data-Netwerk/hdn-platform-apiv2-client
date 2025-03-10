package org.hdn.api;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * Implements the API connection to the HDN Platform of Trust
 */
public class APIController {
    /**
     * Holds the instance for the singleton pattern
     */
    private static APIController instance;

    /**
     * Returns the singleton instance of the APIController
     *
     * @return the singleton instance of the APIController
     */
    public static APIController getInstance() {
        if (instance == null) instance = new APIController();
        return instance;
    }

    /**
     * Builds the full URI for the request
     *
     * @param baseUrl the base URL which defined the environment to use
     * @param params  the list with parameters
     * @return the full URI
     * @throws URISyntaxException thrown when an error occurs creating the URI
     */
    public static String buildUrl(String baseUrl, Map<String, String> params) throws URISyntaxException {
        StringJoiner queryJoiner = new StringJoiner("&");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value != null) {
                String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
                String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);

                queryJoiner.add(encodedKey + "=" + encodedValue);
            }
        }

        String queryString = queryJoiner.toString();
        URI uri = new URI(baseUrl + (queryString.isEmpty() ? "" : "?" + queryString));
        return uri.toString();
    }

    private final Logger logger = LoggerFactory.getLogger(APIController.class);
    private String baseURL;
    private String authURL;
    private String clientID;
    private String clientSecret;
    private String accessToken;
    private String keyStorePath;
    private String keyStorePassword;
    private Properties props;
    private SSLContext sslContext;

    /**
     * Constructs the APIController
     */
    public APIController() {
        try {
            props = new Properties();
            props.load(new FileInputStream("settings.properties"));
            baseURL = props.getProperty("baseURL");
            authURL = props.getProperty("authURL");
            clientID = props.getProperty("clientID");
            clientSecret = props.getProperty("clientSecret");
            keyStorePath = props.getProperty("certificate");
            keyStorePassword = props.getProperty("password");

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            FileInputStream fis = new FileInputStream(keyStorePath);
            keyStore.load(fis, keyStorePassword.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyStorePassword.toCharArray());
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
        } catch (UnrecoverableKeyException | CertificateException | KeyStoreException | IOException |
                 NoSuchAlgorithmException | KeyManagementException e) {
            logger.error("Error constructor: {}", e.getMessage());
        }
    }

    /**
     * Extracts the first privatekey of the certificate configured in the settings.properties based on the
     * password configured in the settings.properties
     *
     * @return the private key or null of no private keys are found
     * @throws IOException               thrown when an IO error occurs
     * @throws KeyStoreException         thrown when an error in the keystore occurs
     * @throws CertificateException      thrown when an error in the certificate occurs
     * @throws NoSuchAlgorithmException  thrown when an error in the algorithm occurs
     * @throws UnrecoverableKeyException thrown when an unrecoverable key error occurs
     */
    public PrivateKey getPrivateKey() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        FileInputStream fis = new FileInputStream(keyStorePath);
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(fis, keyStorePassword.toCharArray());

        Enumeration<String> aliases = keystore.aliases();
        if (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            return (PrivateKey) keystore.getKey(alias, keyStorePassword.toCharArray());
        } else {
            logger.error("Geen alias gevonden in het .p12 bestand.");
        }
        return null;
    }

    /**
     * Requests a new token and stores it for usage in future requests
     */
    public void getToken() {
        String form = "grant_type=password&" +
                "client_id=" + clientID + "&" +
                "client_secret=" + clientSecret + "&" +
                "scope=openid profile";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authURL + "/auth/realms/platformoftrust/protocol/openid-connect/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject obj = new JSONObject(response.body());
            accessToken = obj.getString("access_token");
        } catch (InterruptedException | IOException e) {
            logger.info("{}", e.getMessage());
        }
    }

    /**
     * Performs a get call
     *
     * @param URL the URL to call, without the baseURL part
     * @return returns the APIResponse
     * @throws IOException          thrown when an IO error occurs
     * @throws InterruptedException thrown when the request is interruped
     */
    public APIResponse get(String URL) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + URL))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try (HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new APIResponse(response);
        } catch (IOException | InterruptedException e) {
            logger.error("{}", e.getMessage());
            throw e;
        }
    }

    /**
     * Performs a post call without parameters
     *
     * @param URL the URL to call, without the baseURL part
     * @return returns the APIResponse
     * @throws IOException          thrown when an IO error occurs
     * @throws InterruptedException thrown when the request is interruped
     */
    public APIResponse post(String URL) throws IOException, InterruptedException {
        return post(URL, null);
    }

    /**
     * /**
     * Performs a post call without parameters
     *
     * @param URL  the URL to call, without the baseURL part
     * @param body the body to use in the call
     * @return returns the APIResponse
     * @throws IOException          thrown when an IO error occurs
     * @throws InterruptedException thrown when the request is interruped
     */
    public APIResponse post(String URL, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + URL))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .build();

        try (HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new APIResponse(response);
        } catch (IOException | InterruptedException e) {
            logger.error("{}", e.getMessage());
            throw e;
        }
    }

    /**
     * Returns a property from the settings.properties file or null if the property is not found
     * @param propName the name of the property
     * @return the value of the property
     */
    public String getProp(String propName) {
        return props.getProperty(propName);
    }
}
