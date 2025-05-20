package org.hdn.api.object;

import org.hdn.api.APIConstants;
import org.hdn.api.APIController;
import org.hdn.api.APIResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
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
        resourceUuid = attributes.getString("resourceUuid");
        url = attributes.getString("url");
        messageTypes = attributes.getJSONArray("messageTypes").toList().stream().map(Object::toString).toArray(String[]::new);
        nodes = attributes.getJSONArray("nodes").toList().stream().map(Object::toString).toArray(String[]::new);
        authenticationMethod = attributes.getString("authenticationMethod");
        certificateUuid = attributes.optString("certificateUuid", null);
        sub = attributes.getString("sub");
        creationDate = Instant.parse(attributes.getString("creationDate"));
    }

    public APIResponse create() throws IOException, InterruptedException {
        if (this.resourceUuid == null) {
            JSONObject body = new JSONObject();
            body.put("url", url);
            body.put("messageTypes", new JSONArray(Objects.requireNonNullElse(messageTypes, new String[0])));
            body.put("nodes", new JSONArray(nodes));
            if (authenticationMethod != null) body.put("authenticationMethod", authenticationMethod);
            if (certificateUuid != null) body.put("certificateUuid", certificateUuid);

            APIResponse apiResponse = APIController.getInstance().post(String.format(APIConstants.HOOK_CREATE), body.toString());
            if (apiResponse.getResponse().statusCode() == 201) {
                updateAttributes(apiResponse.getBody());
            }
            return apiResponse;
        } else {
            logger.error("Cannot create when resourceUuid is not null, use update instead");
        }
        return null;
    }

    public APIResponse update() throws IOException, InterruptedException {
        if (this.resourceUuid != null) {
            JSONObject body = new JSONObject();
            body.put("url", url);
            body.put("messageTypes", new JSONArray(Objects.requireNonNullElse(messageTypes, new String[0])));
            body.put("nodes", new JSONArray(nodes));
            if (authenticationMethod != null) body.put("authenticationMethod", authenticationMethod);
            if (certificateUuid != null) body.put("certificateUuid", certificateUuid);

            APIResponse apiResponse = APIController.getInstance().put(String.format(APIConstants.HOOK_PUT, this.resourceUuid), body.toString());
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
    public Hook fetch() throws IOException, InterruptedException {
        APIResponse apiResponse = APIController.getInstance().get(String.format(APIConstants.HOOK_GET, resourceUuid));

        if (apiResponse.getResponse().statusCode() == 200) {
            updateAttributes(apiResponse.getBody());
        }
        return this;
    }

    public APIResponse delete() throws IOException, InterruptedException {
        logger.info("Deleting hook with resource uuid {}", resourceUuid);
        APIResponse apiResponse = APIController.getInstance().delete(String.format(APIConstants.HOOK_DELETE, resourceUuid));
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
