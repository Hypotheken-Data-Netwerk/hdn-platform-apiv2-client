package org.hdn.api.object;

import org.hdn.api.APIConstants;
import org.hdn.api.APIController;
import org.hdn.api.APIResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * Represents a dossier object on the HDN Platform Of Trust
 */
public class Dossier extends APIObject {
    private List<String> originalNodes;
    private List<String> nodes;
    private String resourceUuid;
    private String sub;
    private String requestTraceNr;
    private String sortedOriginalNodes;
    private Instant creationDate;
    private RecordList recordList;
    private EventList eventList;

    /**
     * Construct a new dossier
     */
    public Dossier() {
    }

    /**
     * Construct an existing dossier
     *
     * @param resourceUuid The uuid of the dossier
     */
    public Dossier(String resourceUuid) {
        this.resourceUuid = resourceUuid;
        recordList = new RecordList(resourceUuid);
        eventList = new EventList(resourceUuid);
    }

    /**
     * Updates the attributes, based on the JSON object returned by the platform
     *
     * @param attributes a collection of attributes returned by the platform
     */
    @SuppressWarnings("unused")
    private void updateAttributes(JSONObject attributes) {
        originalNodes = attributes.getJSONArray("originalNodes").toList().stream().map(Object::toString).toList();
        nodes = attributes.getJSONArray("nodes").toList().stream().map(Object::toString).toList();
        resourceUuid = attributes.getString("resourceUuid");
        sub = attributes.optString("sub");
        requestTraceNr = attributes.optString("requestTraceNr");
        sortedOriginalNodes = attributes.optString("sortedOriginalNodes");
        creationDate = Instant.parse(attributes.getString("creationDate"));
        recordList = new RecordList(resourceUuid);
        eventList = new EventList(resourceUuid);
    }

    /**
     * Creates a dossier on the platform when the dossier has not been created yet
     *
     * @return When a dossier is non-existing the result of the response of the platform, otherwise null
     * @throws IOException          exception thrown when an IO error has occured
     * @throws InterruptedException exception thrown when the API request to the platform was interrupted
     */
    @SuppressWarnings("unused")
    public APIResponse create() throws IOException, InterruptedException {
        // If the resource is not created
        if (resourceUuid == null) {
            // Process the post call
            APIResponse APIResponse = APIController.getInstance().post(APIConstants.DOSSIER_CREATE);

            // When a dossier is created
            if (APIResponse.getResponse().statusCode() == 201) {
                // Update the attributes
                updateAttributes(APIResponse.getBody());
            } else {
                logger.error("Error occured while creating a dossier: {}", APIResponse.getResponse().body());
            }
            return APIResponse;
        } else {
            logger.debug("Dossier already created.");
        }
        return null;
    }

    /**
     * Fetches a dossier
     *
     * @return The dossier object
     * @throws IOException          exception thrown when an IO error has occured
     * @throws InterruptedException exception thrown when the API request to the platform was interrupted
     */
    @SuppressWarnings("unused")
    public Dossier fetch() throws IOException, InterruptedException {
        if (resourceUuid != null) {
            // Process the get call
            APIResponse APIResponse = APIController.getInstance().get(String.format(APIConstants.DOSSIER_GET, resourceUuid));

            // When the dossier is returned
            if (APIResponse.getResponse().statusCode() == 200) {
                // Update the attributes
                updateAttributes(APIResponse.getBody());
            } else {
                logger.error("Error occured while fetching a dossier: {}", APIResponse.getResponse().body());
            }
        } else {
            logger.error("Cannot fetch: dossier not created yet");
        }
        return this;
    }

    /**
     * Adds a node to the dossier on the HDN Platform Of Trust
     *
     * @param node Node to be added
     * @return The dossier object
     * @throws IOException          exception thrown when an IO error has occured
     * @throws InterruptedException exception thrown when the API request to the platform was interrupted
     */
    @SuppressWarnings("unused")
    public Dossier addNode(String node) throws IOException, InterruptedException {
        if (resourceUuid != null) {
            JSONObject body = new JSONObject();
            body.put("node", node);

            // Process the post call
            APIResponse APIResponse = APIController.getInstance().post(String.format(APIConstants.DOSSIER_ADD_NODE, resourceUuid), body.toString());

            // When a dossier is created
            if (APIResponse.getResponse().statusCode() == 200) {
                // Update the attributes
                updateAttributes(APIResponse.getBody());
            } else {
                logger.error("Error occured while adding a node: {}", APIResponse.getResponse().body());
            }
        } else {
            logger.error("Cannot add a node: dossier not created yet");
        }
        return this;
    }

    /**
     * When the dossier exists on the HDN Platform of Trust, all original nodes with access
     * to the dossier are returned, otherwise null
     *
     * @return A list with all original nodes with access to the dossier or null
     */
    @SuppressWarnings("unused")
    public List<String> getOriginalNodes() {
        return originalNodes;
    }

    /**
     * When the dossier exists on the HDN Platform of Trust, all nodes with access to the dossier
     * are returned, otherwise null
     *
     * @return A list with all nodes with access to the dossier
     */
    @SuppressWarnings("unused")
    public List<String> getNodes() {
        return nodes;
    }

    /**
     * When the dossier exists on the HDN Platform of Trust, the resourceUUID of the dossier
     * is returned, otherwise null
     *
     * @return the resource UUID or null
     */
    @SuppressWarnings("unused")
    public String getResourceUuid() {
        return resourceUuid;
    }

    /**
     * When the dossier exists on the HDN Platform of Trust, the sub of the dossier
     * is returned, otherwise null
     *
     * @return the sub or null
     */
    @SuppressWarnings("unused")
    public String getSub() {
        return sub;
    }

    /**
     * When the dossier exists on the HDN Platform of Trust, a list with the original nodes
     * of the dossier sorted by node is returned, otherwise null
     *
     * @return the nodes or null
     */
    @SuppressWarnings("unused")
    public String getSortedOriginalNodes() {
        return sortedOriginalNodes;
    }

    /**
     * When the dossier exists on the HDN Platform of Trust, the creation date
     * of the dossier is returned, otherwise null
     *
     * @return the date of creation of the dossier
     */
    @SuppressWarnings("unused")
    public Instant getCreationDate() {
        return creationDate;
    }

    /**
     * When the dossier exists on the HDN Platform of Trust, the RecordList object
     * of the dossier is returned, otherwise null
     *
     * @return the RecordList or null
     */
    @SuppressWarnings("unused")
    public RecordList getRecordList() {
        return recordList;
    }

    /**
     * Returns the EventList of the dossier
     *
     * @return the eventList object
     */
    @SuppressWarnings("unused")
    public EventList getEventList() {
        return eventList;
    }

    /**
     * When the dossier exists on the HDN Platform of Trust, the requestTraceNr
     * of the dossier is returned, otherwise null
     * Remark: the requestTraceNr is deprecated and will be removed.
     *
     * @return the requestTraceNr or null
     */
    @Deprecated
    @SuppressWarnings("unused")
    public String getRequestTraceNr() {
        logger.info("Use of requestTraceNr is deprecated.");
        return requestTraceNr;
    }
}