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
     * Initializes the instance
     *
     * @param baseURL          the base URL for the API calls
     * @param authURL          the base URL for the Authorization (token) calls
     * @param clientID         the client ID used for the Authorization
     * @param clientSecret     the client secret used for the Authorization
     * @param keyStorePath     the path to the certificate
     * @param keyStorePassword the passwordt of the certificate
     */
    public static synchronized void init(String baseURL, String authURL, String clientID, String clientSecret, String keyStorePath, String keyStorePassword) {
        if (instance != null) {
            throw new IllegalStateException("APIController already initialized");
        }
        instance = new APIController(baseURL, authURL, clientID, clientSecret, keyStorePath, keyStorePassword);
    }

    /**
     * Checks if the singleton instance is already initialized
     *
     * @return true if the instsance is initiatilized, false if not
     */
    public static boolean isNotInitialized() {
        return instance == null;
    }

    /**
     * Returns the singleton instance of the APIController
     *
     * @return the singleton instance of the APIController
     */
    public static APIController getInstance() {
        if (instance == null) throw new IllegalStateException("APIController not initialized");
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

    private static final Logger logger = LoggerFactory.getLogger(APIController.class);
    private final String baseURL;
    private final String authURL;
    private final String clientID;
    private final String clientSecret;
    private final String keyStorePath;
    private final String keyStorePassword;

    private String accessToken;
    private SSLContext sslContext;

    /**
     * Constructs the APIController
     */
    public APIController(String baseURL, String authURL, String clientID, String clientSecret, String keyStorePath, String keyStorePassword) {
        this.baseURL = baseURL;
        this.authURL = authURL;
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;

        try (FileInputStream fis = new FileInputStream(keyStorePath)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
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
     * Extracts the first private key of the certificate configured in the settings.properties
     * based on the password configured in the settings.properties.
     *
     * @return the private key
     * @throws IOException               thrown when an IO error occurs
     * @throws KeyStoreException         thrown when an error in the keystore occurs
     * @throws CertificateException      thrown when an error in the certificate occurs
     * @throws NoSuchAlgorithmException  thrown when an error in the algorithm occurs
     * @throws UnrecoverableKeyException thrown when an unrecoverable key error occurs
     */
    public PrivateKey getPrivateKey() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        try (FileInputStream fis = new FileInputStream(keyStorePath)) {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(fis, keyStorePassword.toCharArray());

            Enumeration<String> aliases = keystore.aliases();
            if (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Key key = keystore.getKey(alias, keyStorePassword.toCharArray());

                if (key instanceof PrivateKey privateKey) {
                    return privateKey;
                } else {
                    logger.error("De sleutel onder alias '{}' is geen PrivateKey.", alias);
                    throw new KeyStoreException("Gevonden sleutel is geen PrivateKey.");
                }
            } else {
                logger.error("Geen alias gevonden in het .p12 bestand.");
                throw new KeyStoreException("Geen alias gevonden in het key store.");
            }
        }
    }

    /**
     * Extracts the first public key from the certificate stored in the configured .p12 file.
     *
     * @return the PublicKey
     * @throws IOException              if an I/O error occurs
     * @throws KeyStoreException        if the keystore has a problem
     * @throws CertificateException     if the certificate can't be loaded
     * @throws NoSuchAlgorithmException if the algorithm is unsupported
     */
    public PublicKey getPublicKey() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        try (FileInputStream fis = new FileInputStream(keyStorePath)) {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(fis, keyStorePassword.toCharArray());

            Enumeration<String> aliases = keystore.aliases();
            if (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                java.security.cert.Certificate cert = keystore.getCertificate(alias);

                if (cert != null) {
                    return cert.getPublicKey();
                } else {
                    logger.error("Geen certificaat gevonden voor alias '{}'.", alias);
                    throw new KeyStoreException("Geen certificaat gevonden.");
                }
            } else {
                logger.error("Geen alias gevonden in het .p12 bestand.");
                throw new KeyStoreException("Geen alias gevonden in het keystore.");
            }
        }
    }

    public String getPublicKeyAsPem() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        PublicKey publicKey = getPublicKey();
        byte[] encoded = publicKey.getEncoded();
        String base64Encoded = Base64.getEncoder().encodeToString(encoded);

        StringBuilder pemBuilder = new StringBuilder();
        pemBuilder.append("-----BEGIN PUBLIC KEY-----\n");
        for (int i = 0; i < base64Encoded.length(); i += 64) {
            int endIndex = Math.min(i + 64, base64Encoded.length());
            pemBuilder.append(base64Encoded, i, endIndex).append("\n");
        }
        pemBuilder.append("-----END PUBLIC KEY-----");

        return pemBuilder.toString();
    }


    /**
     * Requests a new token and stores it for usage in future requests
     */
    public void getToken() {
        String form = "grant_type=password&" + "client_id=" + clientID + "&" + "client_secret=" + clientSecret + "&" + "scope=openid profile";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(authURL + "/auth/realms/platformoftrust/protocol/openid-connect/token")).header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form)).build();
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject obj = new JSONObject(response.body());
            accessToken = obj.getString("access_token");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("{}", e.getMessage());
        } catch (IOException e) {
            logger.info("{}", e.getMessage());
        }
    }

    private static HttpRequest.Builder setOnBehalfOf(HttpRequest.Builder builder, String node) {
        if (node != null && !node.isBlank()) {
            if (node.matches("\\d{6}")) {
                builder.header("x-on-behalf-of", node);
            } else {
                logger.error("Provided node number {} should be 6 digits", node);
                throw new NumberFormatException("Node number should be 6 digits");
            }
        }
        return builder;
    }

    private static final HttpClient client = HttpClient.newBuilder()
            //.version(HttpClient.Version.HTTP_1_1) // of HTTP_2 als stabiel
            .build();

    /**
     * Performs a get call
     *
     * @param url the URL to call, without the baseURL part
     * @return returns the APIResponse
     * @throws IOException          thrown when an IO error occurs
     * @throws InterruptedException thrown when the request is interruped
     */
    public APIResponse get(String url) throws IOException, InterruptedException {
        return get(url, null);
    }

    /**
     * Performs a get call
     *
     * @param url  the URL to call, without the baseURL part
     * @param node the node on behalf of which the request is made or null
     * @return returns the APIResponse
     * @throws IOException          thrown when an IO error occurs
     * @throws InterruptedException thrown when the request is interruped
     */
    public APIResponse get(String url, String node) throws IOException, InterruptedException {
        HttpRequest request = setOnBehalfOf(
                HttpRequest.newBuilder().uri(
                                URI.create(baseURL + url))
                        .header(APIConstants.HEADER_AUTH, APIConstants.AUTH_HEADER_PREFIX + accessToken)
                        .header(APIConstants.HEADER_CONTENT_TYPE, APIConstants.CONTENT_TYPE_HEADER)
                        .GET(),
                node)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new APIResponse(response);
    }

    /**
     * Performs a post call without parameters
     *
     * @param url the URL to call, without the baseURL part
     * @return returns the APIResponse
     * @throws IOException          thrown when an IO error occurs
     * @throws InterruptedException thrown when the request is interruped
     */
    public APIResponse post(String url, String node) throws IOException, InterruptedException {
        return post(url, null, node);
    }

    /**
     * Performs a post call with parameters
     *
     * @param url  the URL to call, without the baseURL part
     * @param body the body to use in the call
     * @param node the node on behalf of which the request is made or null
     * @return returns the APIResponse
     * @throws IOException          thrown when an IO error occurs
     * @throws InterruptedException thrown when the request is interruped
     */
    public APIResponse post(String url, String body, String node) throws IOException, InterruptedException {
        HttpRequest request = setOnBehalfOf(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseURL + url))
                        .header(APIConstants.HEADER_AUTH, APIConstants.AUTH_HEADER_PREFIX + accessToken)
                        .header(APIConstants.HEADER_CONTENT_TYPE, APIConstants.CONTENT_TYPE_HEADER)
                        .POST(body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)),
                node)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new APIResponse(response);
    }

    /**
     * Performs a put call with parameters
     *
     * @param url  the URL to call, without the baseURL part
     * @param node the node on behalf of which the request is made or null
     * @return returns the APIResponse
     * @throws IOException          thrown when an IO error occurs
     * @throws InterruptedException thrown when the request is interruped
     */
    public APIResponse put(String url, String node) throws IOException, InterruptedException {
        return put(url, null, node);
    }

    /**
     * Performs a put call with parameters
     *
     * @param url  the URL to call, without the baseURL part
     * @param body the body to use in the call
     * @param node the node on behalf of which the request is made or null
     * @return returns the APIResponse
     * @throws IOException          thrown when an IO error occurs
     * @throws InterruptedException thrown when the request is interruped
     */
    public APIResponse put(String url, String body, String node) throws IOException, InterruptedException {
        HttpRequest request = setOnBehalfOf(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseURL + url))
                        .header(APIConstants.HEADER_AUTH, APIConstants.AUTH_HEADER_PREFIX + accessToken)
                        .header(APIConstants.HEADER_CONTENT_TYPE, APIConstants.CONTENT_TYPE_HEADER)
                        .PUT(body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)),
                node)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new APIResponse(response);
    }

    /**
     * Performs a delete call without parameters
     *
     * @param url the URL to call, without the baseURL part
     * @return returns the APIResponse
     * @throws IOException          thrown when an IO error occurs
     * @throws InterruptedException thrown when the request is interruped
     */
    public APIResponse delete(String url) throws IOException, InterruptedException {
        return delete(url, null);
    }

    /**
     * Performs a delete call without parameters
     *
     * @param url  the URL to call, without the baseURL part
     * @param node the node on behalf of which the request is made or null
     * @return returns the APIResponse
     * @throws IOException          thrown when an IO error occurs
     * @throws InterruptedException thrown when the request is interruped
     */
    public APIResponse delete(String url, String node) throws IOException, InterruptedException {
        HttpRequest request = setOnBehalfOf(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseURL + url))
                        .header(APIConstants.HEADER_AUTH, APIConstants.AUTH_HEADER_PREFIX + accessToken)
                        .header(APIConstants.HEADER_CONTENT_TYPE, APIConstants.CONTENT_TYPE_HEADER).DELETE(),
                node)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new APIResponse(response);
    }
}
