package org.hdn.api.object;

import org.hdn.api.APIConstants;
import org.hdn.api.APIController;
import org.hdn.api.APIResponse;
import org.json.JSONObject;

import java.io.IOException;

public class PublicKey extends APIObject {
    private String resourceUuid;
    private String node;
    private String algorithm;
    private String publickey;
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
        publickey = attributes.getString("publicKey");
        sub = attributes.getString("sub");
    }

    public APIResponse create() throws IOException, InterruptedException {
        if (this.resourceUuid == null) {
            JSONObject data = new JSONObject();
            data.put("algorithm", algorithm);
            data.put("publicKey", publickey);

            JSONObject body = new JSONObject();
            body.put("data", data);

            APIResponse apiResponse = APIController.getInstance().post(String.format(APIConstants.PUBLIC_KEY_CREATE), body.toString());
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
    public PublicKey fetch() throws IOException, InterruptedException {
        APIResponse apiResponse = APIController.getInstance().get(String.format(APIConstants.PUBLIC_KEY_GET, resourceUuid));

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

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getPublickey() {
        return publickey;
    }

    public void setPublickey(String publickey) {
        this.publickey = publickey;
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
