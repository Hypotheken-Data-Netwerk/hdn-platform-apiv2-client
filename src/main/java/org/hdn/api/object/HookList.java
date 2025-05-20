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

public class HookList extends APIObject {
    private final List<Hook> hooks = new ArrayList<>();

    /**
     * The limit of hooks to retrieve in a single call
     */
    private Integer limit = 100;
    /**
     * The offset of the list of hooks to retrieve in a single call
     */
    private Integer offset = 0;
    /**
     * The resourceUuid of the hooks to retrieve
     */
    private String resourceUuid;
    /**
     * The creationdate timestamp of the hooks to retrieve
     */
    private String timestamp = null;
    /**
     * The operator to use to compare against the creationdate
     */
    private String timestampOperator = null;
    private String messageTypes = null;

    @SuppressWarnings("unused")
    public HookList() {

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
    public HookList get() throws IOException, URISyntaxException, InterruptedException, JSONException {
        try {
            hooks.clear();
            Integer total = 0;
            Integer offset = this.offset;

            while (offset <= total) {
                Map<String, String> params = buildParams(offset);

                // Process the get call
                String uri = String.format(APIConstants.HOOKS_GET);
                logger.info(APIController.buildUrl(uri, params));
                APIResponse APIResponse = APIController.getInstance().get(APIController.buildUrl(uri, params));

                // When the list of dossiers is returned
                if (APIResponse.getResponse().statusCode() == 200) {
                    JSONArray records = APIResponse.getBody().getJSONObject("data").getJSONArray("hooks");
                    for (Object record : records) {
                        Hook tmp = new Hook(((JSONObject) record).getString("resourceUuid"), record.toString());
                        this.hooks.add(tmp);
                    }

                    total = this.hooks.isEmpty() ? -1 : APIResponse.getBody().getInt("total");
                    offset += limit;
                } else {
                    logger.error("Error with code [{}] while retrieving the hooklist", APIResponse.getResponse().statusCode());
                    total = -1;
                }
            }
        } catch (IOException | InterruptedException | URISyntaxException | JSONException e) {
            logger.error("Exception occured while retrieving the hooklist: {}", e.getMessage());
            throw e;
        }

        return this;
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
        if (timestamp != null)
            params.put("timestamp" + ((timestampOperator == null) ? "" : "[" + timestampOperator + "]"), timestamp);
        if (resourceUuid != null) params.put("resourceUuid", resourceUuid);
        params.put("messageTypes", Objects.requireNonNullElse(messageTypes, "[]"));
        return params;
    }

    /**
     * Returns all retrieved hooks as a list
     *
     * @return a list of the retrieved hooks
     */
    @SuppressWarnings("unused")
    public List<Hook> getHooks() {
        return hooks;
    }

    /**
     * Sets the limit of hooks to retrieve in a single call
     *
     * @param limit the limit, must be greater than 0 and smaller or equal to 1000
     * @return the hook list object itself
     */
    @SuppressWarnings("unused")
    public HookList setLimit(Integer limit) {
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
    public HookList setOffset(Integer offset) {
        if (offset >= 0) {
            this.offset = offset;
        } else {
            logger.error("Offset should be greater than or equal to 0.");
        }
        return this;
    }

    /**
     * Sets the filter parameters for the filtering on the creationdate
     *
     * @param operator  how to compare the creationDate, valid values are "$lt", "$lte", "$ne", "$gte", "$gt"
     * @param timestamp the creationDate to compare against
     * @return the hook list object itself
     */
    @SuppressWarnings("unused")
    public HookList setTimestamp(String operator, String timestamp) {
        List<String> operators = List.of("$lt", "$lte", "$ne", "$gte", "$gt");
        if (operators.contains(operator) || operator == null) {
            this.timestampOperator = operator;
            this.timestamp = timestamp;
        } else {
            logger.error("Operator should be one of {}.", operators);
        }
        return this;
    }

    /**
     * Sets the resource UUID to filter the records on
     *
     * @param resourceUuid the UUID to filter on
     * @return the hook object list itself
     */
    @SuppressWarnings("unused")
    public HookList setResourceUuid(String resourceUuid) {
        this.resourceUuid = resourceUuid;
        return this;
    }

    /**
     * Sets the messageTypes to filter the records on
     *
     * @param messageTypes the messageTypes to filter on
     * @return the hook object list itself
     */
    @SuppressWarnings("unused")
    public HookList setMessageTypes(String messageTypes) {
        this.messageTypes = messageTypes;
        return this;
    }
}