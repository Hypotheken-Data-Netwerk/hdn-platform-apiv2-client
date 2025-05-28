package org.hdn.api.object;

import org.hdn.api.APIConstants;
import org.hdn.api.APIController;
import org.hdn.api.APIResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class PublicKeyList extends APIObject {
    private final List<PublicKey> publickeys = new ArrayList<>();

    /**
     * The limit of publickeys to retrieve in a single call
     */
    private Integer limit = 100;
    /**
     * The offset of the list of publickeys to retrieve in a single call
     */
    private Integer offset = 0;
    /**
     * The node of the list of publickeys to retrieve
     */
    private String node = null;

    @SuppressWarnings("unused")
    public PublicKeyList() {
    }

    /**
     * Retrieves all hooks based on the parameters and filter provided
     *
     * @return the HookList object itself
     * @throws IOException          thrown when an IO error occurs
     * @throws URISyntaxException   thrown when a URI syntax error occurs
     * @throws InterruptedException thrown when an interrupted error occurs
     * @throws JSONException        thrown when an error occurs in parsing the JSON
     */
    @SuppressWarnings("unused,UnusedReturnValue")
    public PublicKeyList get() throws IOException, URISyntaxException, InterruptedException, JSONException {
        try {
            publickeys.clear();
            Integer total = 0;
            Integer offset = this.offset;

            while (offset <= total) {
                Map<String, String> params = buildParams(offset);

                // Process the get call
                String uri = String.format(APIConstants.PUBLIC_KEYS_GET);
                logger.info(APIController.buildUrl(uri, params));
                APIResponse APIResponse = APIController.getInstance().get(APIController.buildUrl(uri, params));

                // When the list of dossiers is returned
                if (APIResponse.getResponse().statusCode() == 200) {
                    JSONArray records = APIResponse.getBody().getJSONObject("data").getJSONArray("publickeys");
                    for (Object record : records) {
                        PublicKey tmp = new PublicKey(((JSONObject) record).getString("resourceUuid"));
                        this.publickeys.add(tmp);
                    }

                    total = this.publickeys.isEmpty() ? -1 : APIResponse.getBody().getInt("total");
                    offset += limit;
                } else {
                    logger.error("Error with code [{}] while retrieving the publickeys list", APIResponse.getResponse().statusCode());
                    total = -1;
                }
            }
        } catch (IOException | InterruptedException | URISyntaxException | JSONException e) {
            logger.error("Exception occured while retrieving the publickeys list: {}", e.getMessage());
            throw e;
        }

        return this;
    }

    /**
     * Retrieves all supported algorithms
     *
     * @return a list of supported algorithms
     * @throws IOException          thrown when an IO error occurs
     * @throws URISyntaxException   thrown when a URI syntax error occurs
     * @throws InterruptedException thrown when an interrupted error occurs
     * @throws JSONException        thrown when an error occurs in parsing the JSON
     */
    @SuppressWarnings("unused,UnusedReturnValue")
    public List<String> getAlgorithms() throws IOException, URISyntaxException, InterruptedException, JSONException {
        List<String> algorithms = new ArrayList<>();
        try {
            // Process the get call
            String uri = String.format(APIConstants.PUBLIC_KEY_ALGORITHM);
            APIResponse APIResponse = APIController.getInstance().get(APIController.buildUrl(uri, new HashMap<>()));

            // When the list of dossiers is returned
            if (APIResponse.getResponse().statusCode() == 200) {
                JSONArray results = APIResponse.getBody().getJSONArray("algorithms");
                for (Object result : results) {
                    algorithms.add(result.toString());
                }
            }
        } catch (IOException | InterruptedException | URISyntaxException | JSONException e) {
            logger.error("Exception occured while retrieving the algorithms list: {}", e.getMessage());
            throw e;
        }

        return algorithms;
    }

    /**
     * Creates the parameters to use during the retrieval of the eventsd
     *
     * @param offset the start offset of the list of events
     * @return the key value based list of parameters
     */
    @SuppressWarnings("unused")
    private Map<String, String> buildParams(Integer offset) {
        Map<String, String> params = new HashMap<>();
        params.put("limit", Integer.toString(limit));
        params.put("offset", Integer.toString(offset));
        if (node != null) params.put("node", node);
        return params;
    }

    /**
     * Returns all retrieved hooks as a list
     *
     * @return a list of the retrieved hooks
     */
    @SuppressWarnings("unused")
    public List<PublicKey> getPublicKeys() {
        return publickeys;
    }

    /**
     * Sets the limit of hooks to retrieve in a single call
     *
     * @param limit the limit, must be greater than 0 and smaller or equal to 1000
     * @return the hook list object itself
     */
    @SuppressWarnings("unused")
    public PublicKeyList setLimit(Integer limit) {
        if (limit > 0 && limit <= 1000) {
            this.limit = limit;
        } else {
            logger.error("Limit should be greater than 0 and lower than or equal to 1000.");
        }
        return this;
    }

    /**
     * Sets the offset of hooks to start retrieving of
     *
     * @param offset the offset of the list of hook, must be greater or equal to 0
     * @return the hook list object itself
     */
    @SuppressWarnings("unused")
    public PublicKeyList setOffset(Integer offset) {
        if (offset >= 0) {
            this.offset = offset;
        } else {
            logger.error("Offset should be greater than or equal to 0.");
        }
        return this;
    }

    /**
     * Sets the node to filter the records on
     *
     * @param node the node to filter on
     * @return the hook object list itself
     */
    @SuppressWarnings("unused")
    public PublicKeyList setNode(String node) {
        this.node = node;
        return this;
    }
}