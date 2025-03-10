package org.hdn.api.object;

import org.hdn.api.APIConstants;
import org.hdn.api.APIController;
import org.hdn.api.APIResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventList extends APIObject {
    private final List<Event> events = new ArrayList<>();

    private final String dossierUuid;
    private String recordUuid;
    private String resourceUuid;
    /**
     * The limit of dossiers to retrieve in a single call
     */
    private Integer limit = 100;
    /**
     * The offset of the list of dossier to retrieve in a single call
     */
    private Integer offset = 0;
    /**
     * The creationdate timestamp of the records to retrieve
     */
    private String timestamp = null;
    /**
     * The operator to use to compare against the creationdate
     */
    private String timestampOperator = null;

    @SuppressWarnings("unused")
    public EventList(String dossierUuid) {
        this.dossierUuid = dossierUuid;
    }

    @SuppressWarnings("unused")
    public EventList(String dossierUuid, String recordUuid) {
        this.dossierUuid = dossierUuid;
        this.recordUuid = recordUuid;
    }

    /**
     * Retrieves all events based on the parameters and filter provided
     *
     * @return the EventList object itself
     * @throws IOException          thrown when an IO error occurs
     * @throws URISyntaxException   thrown when a URI syntax error occurs
     * @throws InterruptedException thrown when an interrupted error occurs
     * @throws JSONException        thrown when an error occurs in parsing the JSON
     */
    @SuppressWarnings("unused,UnusedReturnValue")
    public EventList get() throws IOException, URISyntaxException, InterruptedException, JSONException {
        try {
            events.clear();
            Integer total = 0;
            Integer offset = this.offset;

            while (offset <= total) {
                Map<String, String> params = buildParams(offset);

                // Process the get call
                String uri = this.recordUuid == null ? String.format(APIConstants.DOSSIER_GET_EVENTS, dossierUuid) : String.format(APIConstants.RECORD_GET_EVENTS, dossierUuid, recordUuid);
                logger.info(uri);
                APIResponse APIResponse = APIController.getInstance().get(APIController.buildUrl(uri, params));

                // When the list of dossiers is returned
                if (APIResponse.getResponse().statusCode() == 200) {
                    JSONArray records = APIResponse.getBody().getJSONObject("data").getJSONArray("events");
                    for (Object record : records) {
                        Event tmp = new Event(dossierUuid, recordUuid, ((JSONObject) record).getString("resourceUuid"), record.toString());
                        this.events.add(tmp);
                    }

                    total = this.events.isEmpty() ? -1 : APIResponse.getBody().getInt("total");
                    offset += limit;
                } else {
                    logger.error("Error with code [{}] while retrieving the eventlist", APIResponse.getResponse().statusCode());
                    total = -1;
                }
            }
        } catch (IOException | InterruptedException | URISyntaxException | JSONException e) {
            logger.error("Exception occured while retrieving the eventlist: {}", e.getMessage());
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
        return params;
    }

    /**
     * Returns all retrieved events as a list
     *
     * @return a list of the retrieved events
     */
    @SuppressWarnings("unused")
    public List<Event> getEvents() {
        return events;
    }

    /**
     * Sets the limit of events to retrieve in a single call
     *
     * @param limit the limit, must be greater than 0 and smaller or equal to 1000
     * @return the event list object itself
     */
    @SuppressWarnings("unused")
    public EventList setLimit(Integer limit) {
        if (limit > 0 && limit <= 1000) {
            this.limit = limit;
        } else {
            logger.error("Limit should be greater than 0 and lower than or equal to 1000.");
        }
        return this;
    }

    /**
     * Sets the offset of event to start retrieving of
     *
     * @param offset the offset of the list of event, must be greater or equal to 0
     * @return the event list object itself
     */
    @SuppressWarnings("unused")
    public EventList setOffset(Integer offset) {
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
     * @return the event list object itself
     */
    @SuppressWarnings("unused")
    public EventList setTimestamp(String operator, String timestamp) {
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
     * @return the event object list itself
     */
    @SuppressWarnings("unused")
    public EventList setResourceUuid(String resourceUuid) {
        this.resourceUuid = resourceUuid;
        return this;
    }
}