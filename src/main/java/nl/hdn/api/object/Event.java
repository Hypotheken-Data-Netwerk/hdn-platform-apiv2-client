package nl.hdn.api.object;

import nl.hdn.api.APIConstants;
import nl.hdn.api.APIController;
import nl.hdn.api.APIResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;

public class Event extends APIObject {
    private final String dossierUuid;
    private final String recordUuid;
    private final String resourceUuid;
    private String eventType;
    private String sub;
    private String businessKey;
    private Instant timestamp;

    /**
     * Constructs an existing event, without attributes
     *
     * @param dossierUuid  the resourceUuid of the dossier
     * @param recordUuid   the resourceUuid of the record
     * @param resourceUuid the resourceUuid of the event
     */
    @SuppressWarnings("unused")
    public Event(String dossierUuid, String recordUuid, String resourceUuid) {
        this.dossierUuid = dossierUuid;
        this.recordUuid = recordUuid;
        this.resourceUuid = resourceUuid;
    }

    /**
     * Constructs an existing event, with attributes
     *
     * @param dossierUuid  the resourceUuid of the dossier
     * @param recordUuid   the resourceUuid of the record
     * @param resourceUuid the resourceUuid of the event
     * @param attributes   the attributes as JSON string
     */
    @SuppressWarnings("unused")
    public Event(String dossierUuid, String recordUuid, String resourceUuid, String attributes) {
        this.dossierUuid = dossierUuid;
        this.recordUuid = recordUuid;
        this.resourceUuid = resourceUuid;
        updateAttributes(new JSONObject(attributes));
    }

    /**
     * Updates the attributes, based on the JSON object returned by the platform
     *
     * @param attributes a collection of attributes returned by the platform
     */
    private void updateAttributes(JSONObject attributes) {
        this.eventType = attributes.getString("eventType");
        this.sub = attributes.getString("sub");
        this.businessKey = attributes.optString("businessKey");
        this.timestamp = Instant.parse(attributes.getString("timestamp"));
    }

    /**
     * Fetches an event, with the default API controller
     *
     * @return The event object
     * @throws IOException          exception thrown when an IO error has occured
     * @throws InterruptedException exception thrown when the API request to the platform was interrupted
     */
    @SuppressWarnings("unused")
    public Event fetch() throws IOException, InterruptedException {
        return fetch(APIController.getInstance());
    }

    /**
     * Fetches an event
     *
     * @param apiController the controller to be used for the API calls
     * @return The event object
     * @throws IOException          exception thrown when an IO error has occured
     * @throws InterruptedException exception thrown when the API request to the platform was interrupted
     */
    @SuppressWarnings("unused")
    public Event fetch(APIController apiController) throws IOException, InterruptedException {
        if (dossierUuid != null && recordUuid != null && resourceUuid != null) {
            APIResponse apiResponse = apiController.get(String.format(APIConstants.EVENT_GET, dossierUuid, recordUuid, resourceUuid));

            if (apiResponse.getResponse().statusCode() == 200) {
                updateAttributes(apiResponse.getBody());
            }
        } else {
            logger.error("dossierUuid, recordUuid and/or resourceUuid is null, couldn't fetch event");
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

    /**
     * Returns the type of the event
     *
     * @return the eventType
     */
    @SuppressWarnings("unused")
    public String getEventType() {
        return eventType;
    }

    /**
     * Returns the sub of the event
     *
     * @return the sub
     */
    @SuppressWarnings("unused")
    public String getSub() {
        return sub;
    }

    /**
     * Returns the businesskey when present
     *
     * @return the businesskey or null
     */
    @SuppressWarnings("unused")
    public String getBusinessKey() {
        return businessKey;
    }

    /**
     * Returns the timestamp when the event occured
     *
     * @return the timestamp of the event
     */
    @SuppressWarnings("unused")
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the dossier resourceUuid of the event
     *
     * @return the dossier resourceUuid
     */
    @SuppressWarnings("unused")
    public String getDossierUuid() {
        return dossierUuid;
    }

    /**
     * Returns the record resourceUuid of the event, when present
     *
     * @return the record resourceUuid or null
     */
    @SuppressWarnings("unused")
    public String getRecordUuid() {
        return recordUuid;
    }
}
