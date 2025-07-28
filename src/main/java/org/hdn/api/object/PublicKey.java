package org.hdn.api.object;

import org.hdn.api.APIConstants;
import org.hdn.api.APIController;
import org.hdn.api.APIResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidParameterException;

public class PublicKey extends APIObject {
    private String resourceUuid;
    private String node;
    private String algorithm;
    private String publicKeyValue;
    private String sub;

    public PublicKey() {

    }

    /**
     * Constructs an existing publickey, without attributes
     *
     * @param resourceUuid the resourceUuid of the publickey
     */
    @SuppressWarnings("unused")
    public PublicKey(String resourceUuid) {
        this.resourceUuid = resourceUuid;
    }

    /**
     * Constructs an existing publickey, with attributes
     *
     * @param resourceUuid the resourceUuid of the publickey
     * @param attributes   the attributes as JSON string
     */
    @SuppressWarnings("unused")
    public PublicKey(String resourceUuid, String attributes) {
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
        node = attributes.getString("node");
        algorithm = attributes.getString("algorithm");
        publicKeyValue = attributes.getString("publicKey");
        sub = attributes.getString("sub");
    }

    private void validateOnBehalfOf(String onBehalfOf) throws InvalidParameterException {
        if(onBehalfOf==null || !onBehalfOf.matches("\\d{6}")) {
            logger.error("onBehalfOf node is not set or doesn't match 6 digits but required");
            throw new InvalidParameterException("onBehalfOf is required");
        }
    }

    public APIResponse create(String onBehalfOf) throws IOException, InterruptedException {
        if (this.resourceUuid == null) {
            validateOnBehalfOf(onBehalfOf);

            JSONObject data = new JSONObject();
            data.put("algorithm", algorithm);
            data.put("publicKey", publicKeyValue);

            JSONObject body = new JSONObject();
            body.put("data", data);

            APIResponse apiResponse = APIController.getInstance().post(String.format(APIConstants.PUBLIC_KEY_CREATE), body.toString(), onBehalfOf);
            if (apiResponse.getResponse().statusCode() == 201) {
                updateAttributes(apiResponse.getBody());
            }
            return apiResponse;
        } else {
            logger.error("Cannot create when resourceUuid is not null");
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
    public PublicKey fetch(String onBehalfOf) throws IOException, InterruptedException {
        validateOnBehalfOf(onBehalfOf);

        APIResponse apiResponse = APIController.getInstance().get(String.format(APIConstants.PUBLIC_KEY_GET, resourceUuid), onBehalfOf);

        if (apiResponse.getResponse().statusCode() == 200) {
            updateAttributes(apiResponse.getBody());
        }
        return this;
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

    public String getNode() {
        return node;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public PublicKey setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public String getPublicKeyValue() {
        return publicKeyValue;
    }

    public PublicKey setPublicKeyValue(String publicKeyValue) {
        this.publicKeyValue = publicKeyValue;
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
}
