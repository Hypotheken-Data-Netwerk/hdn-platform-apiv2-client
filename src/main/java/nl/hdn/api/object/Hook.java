package nl.hdn.api.object;

import nl.hdn.api.APIConstants;
import nl.hdn.api.APIController;
import nl.hdn.api.APIResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.Objects;

public class Hook extends APIObject {
    private String resourceUuid;
    private String url;
    private String[] messageTypes;
    private String[] nodes;
    private String authenticationMethod;
    private String certificateUuid;
    private String sub;
    private Instant creationDate;

    private static final String FIELD_RESOURCEUUID = "resourceUuid";
    private static final String FIELD_URL = "url";
    private static final String FIELD_MESSAGE_TYPES = "messageTypes";
    private static final String FIELD_NODES = "nodes";
    private static final String FIELD_AUTHENTICATION_METHOD = "authenticationMethod";
    private static final String FIELD_CERTIFICATEUUID = "certificateUuid";
    private static final String FIELD_SUB = "sub";
    private static final String FIELD_CREATIONDATE = "creationDate";

    public Hook() {

    }

    /**
     * Constructs an existing event, without attributes
     *
     * @param resourceUuid the resourceUuid of the event
     */
    @SuppressWarnings("unused")
    public Hook(String resourceUuid) {
        this.resourceUuid = resourceUuid;
    }

    /**
     * Constructs an existing event, with attributes
     *
     * @param resourceUuid the resourceUuid of the event
     * @param attributes   the attributes as JSON string
     */
    @SuppressWarnings("unused")
    public Hook(String resourceUuid, String attributes) {
        this.resourceUuid = resourceUuid;
        updateAttributes(new JSONObject(attributes));
    }

    /**
     * Updates the attributes, based on the JSON object returned by the platform
     *
     * @param attributes a collection of attributes returned by the platform
     */
    private void updateAttributes(JSONObject attributes) {
        resourceUuid = attributes.getString(FIELD_RESOURCEUUID);
        url = attributes.getString(FIELD_URL);
        messageTypes = attributes.getJSONArray(FIELD_MESSAGE_TYPES).toList().stream().map(Object::toString).toArray(String[]::new);
        nodes = attributes.getJSONArray(FIELD_NODES).toList().stream().map(Object::toString).toArray(String[]::new);
        authenticationMethod = attributes.getString(FIELD_AUTHENTICATION_METHOD);
        certificateUuid = attributes.optString(FIELD_CERTIFICATEUUID, null);
        sub = attributes.getString(FIELD_SUB);
        creationDate = Instant.parse(attributes.getString(FIELD_CREATIONDATE));
    }

    private void validateOnBehalfOf(String onBehalfOf) throws InvalidParameterException {
        if (onBehalfOf == null || !onBehalfOf.matches("\\d{6}")) {
            logger.error("onBehalfOf node is not set or doesn't match 6 digits but required");
            throw new InvalidParameterException("onBehalfOf is required");
        }
    }

    public APIResponse create(String onBehalfOf) throws IOException, InterruptedException {
        return create(onBehalfOf, APIController.getInstance());
    }

    public APIResponse create(String onBehalfOf, APIController apiController) throws IOException, InterruptedException {
        if (this.resourceUuid == null) {
            validateOnBehalfOf(onBehalfOf);

            JSONObject body = new JSONObject();
            body.put(FIELD_URL, url);
            body.put(FIELD_MESSAGE_TYPES, new JSONArray(Objects.requireNonNullElse(messageTypes, new String[0])));
            body.put(FIELD_NODES, new JSONArray(nodes));
            if (authenticationMethod != null) body.put(FIELD_AUTHENTICATION_METHOD, authenticationMethod);
            if (certificateUuid != null) body.put(FIELD_CERTIFICATEUUID, certificateUuid);

            APIResponse apiResponse = apiController.post(String.format(APIConstants.HOOK_CREATE), body.toString(), onBehalfOf);
            if (apiResponse.getResponse().statusCode() == 201) {
                updateAttributes(apiResponse.getBody());
            }
            return apiResponse;
        } else {
            logger.error("Cannot create when resourceUuid is not null, use update instead");
        }
        return null;
    }

    public APIResponse update(String onBehalfOf) throws IOException, InterruptedException {
        return update(onBehalfOf, APIController.getInstance());
    }

    public APIResponse update(String onBehalfOf, APIController apiController) throws IOException, InterruptedException {
        if (this.resourceUuid != null) {
            validateOnBehalfOf(onBehalfOf);

            JSONObject body = new JSONObject();
            body.put(FIELD_URL, url);
            body.put(FIELD_MESSAGE_TYPES, new JSONArray(Objects.requireNonNullElse(messageTypes, new String[0])));
            body.put(FIELD_NODES, new JSONArray(nodes));
            if (authenticationMethod != null) body.put(FIELD_AUTHENTICATION_METHOD, authenticationMethod);
            if (certificateUuid != null) body.put(FIELD_CERTIFICATEUUID, certificateUuid);

            APIResponse apiResponse = apiController.put(String.format(APIConstants.HOOK_PUT, this.resourceUuid), body.toString(), onBehalfOf);
            if (apiResponse.getResponse().statusCode() == 200) {
                updateAttributes(apiResponse.getBody());
            }
            return apiResponse;
        } else {
            logger.error("Cannot update when resourceUuid is null, use create instead");
        }
        return null;
    }

    /**
     * Fetches a hook
     *
     * @return The hook object
     * @throws IOException          exception thrown when an IO error has occured
     * @throws InterruptedException exception thrown when the API request to the platform was interrupted
     */
    @SuppressWarnings("unused")
    public Hook fetch(String onBehalfOf) throws IOException, InterruptedException {
        return fetch(onBehalfOf, APIController.getInstance());
    }

    /**
     * Fetches a hook
     *
     * @return The hook object
     * @throws IOException          exception thrown when an IO error has occured
     * @throws InterruptedException exception thrown when the API request to the platform was interrupted
     */
    @SuppressWarnings("unused")
    public Hook fetch(String onBehalfOf, APIController apiController) throws IOException, InterruptedException {
        validateOnBehalfOf(onBehalfOf);

        APIResponse apiResponse = apiController.get(String.format(APIConstants.HOOK_GET, resourceUuid), onBehalfOf);

        if (apiResponse.getResponse().statusCode() == 200) {
            updateAttributes(apiResponse.getBody());
        }
        return this;
    }

    public APIResponse delete(String onBehalfOf) throws IOException, InterruptedException {
        return delete(onBehalfOf, APIController.getInstance());
    }

    public APIResponse delete(String onBehalfOf, APIController apiController) throws IOException, InterruptedException {
        validateOnBehalfOf(onBehalfOf);

        logger.info("Deleting hook with resource uuid {}", resourceUuid);
        APIResponse apiResponse = apiController.delete(String.format(APIConstants.HOOK_DELETE, resourceUuid), onBehalfOf);
        logger.info("Resultcode was {}", apiResponse.getResponse().statusCode());
        if (apiResponse.getResponse().statusCode() == 204) {
            resourceUuid = null;
        }
        return apiResponse;
    }

    /**
     * Returns the resourceUuid of the event
     *
     * @return the resourceUuid
     */
    @SuppressWarnings("unused")
    public String getResourceUuid() {
        return resourceUuid;
    }

    /**
     * Returns the messageTypes of the hook
     *
     * @return the messageTypes
     */
    @SuppressWarnings("unused")
    public String[] getMessageTypes() {
        return messageTypes;
    }

    @SuppressWarnings("unused")
    public Hook setMessageTypes(String[] messageTypes) {
        this.messageTypes = messageTypes;
        return this;
    }

    /**
     * Returns the nodes of the hook
     *
     * @return the nodes
     */
    @SuppressWarnings("unused")
    public String[] getNodes() {
        return nodes;
    }

    public Hook setNodes(String[] nodes) {
        this.nodes = nodes;
        return this;
    }

    /**
     * Returns the authentication method of the hook
     *
     * @return the authentication method
     */
    @SuppressWarnings("unused")
    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public Hook setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
        return this;
    }

    /**
     * Returns the resource Uuid of the certificate of the hook
     *
     * @return the resourceUuid of the certificate
     */
    @SuppressWarnings("unused")
    public String getCertificateUuid() {
        return certificateUuid;
    }

    @SuppressWarnings("unused")
    public Hook setCertificateUuid(String certificateUuid) {
        this.certificateUuid = certificateUuid;
        return this;
    }

    /**
     * Returns the sub of the hook
     *
     * @return the sub
     */
    @SuppressWarnings("unused")
    public String getSub() {
        return sub;
    }

    /**
     * Returns the creation date of the hook
     *
     * @return the creation date
     */
    @SuppressWarnings("unused")
    public Instant getCreationDate() {
        return creationDate;
    }

    /**
     * Returns the URL of the hook
     *
     * @return the URL
     */
    @SuppressWarnings("unused")
    public String getUrl() {
        return url;
    }

    public Hook setUrl(String url) {
        this.url = url;
        return this;
    }
}
