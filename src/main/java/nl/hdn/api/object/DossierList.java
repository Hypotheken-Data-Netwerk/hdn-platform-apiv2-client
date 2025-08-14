package nl.hdn.api.object;

import nl.hdn.api.APIConstants;
import nl.hdn.api.APIController;
import nl.hdn.api.APIResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Represents a list of dossiers on the HDN Platform of Trust
 */
@SuppressWarnings("unused")
public class DossierList extends APIObject {
    /**
     * A list of dossiers
     */
    private final List<Dossier> dossiers = new ArrayList<>();
    /**
     * The limit of dossiers to retrieve in a single call
     */
    private Integer limit = 100;
    /**
     * The offset of the list of dossier to retrieve in a single call
     */
    private Integer offset = 0;
    /**
     * The status of the dossiers to retrieve
     */
    private String status = null;
    /**
     * The sortation of the dossiers
     */
    private String sort = null;
    /**
     * The node that created the dossiers
     */
    private String node = null;
    /**
     * The requesttrace number of the dossiers
     */
    private String requestTraceNr = null;
    /**
     * The UUID of the dossiers
     */
    private String resourceUuid = null;
    /**
     * The original nodes of the dossiers
     */
    private String originalNodes = null;

    /**
     * Retrieves all dossiers based on the parameters and filter provided, with the default API controller
     *
     * @param onBehalfOf the 6-digit nodenumber on behalf of which the request is made
     * @return the DossierList object itself
     * @throws IOException          thrown when an IO error occurs
     * @throws URISyntaxException   thrown when a URI syntax error occurs
     * @throws InterruptedException thrown when an interrupted error occurs
     */
    @SuppressWarnings("unused,UnusedReturnValue")
    public DossierList get(String onBehalfOf) throws IOException, URISyntaxException, InterruptedException {
        return get(onBehalfOf, APIController.getInstance());
    }

    /**
     * Retrieves all dossiers based on the parameters and filter provided
     *
     * @param onBehalfOf    the 6-digit nodenumber on behalf of which the request is made
     * @param apiController the controller to be used for the API calls
     * @return the DossierList object itself
     * @throws IOException          thrown when an IO error occurs
     * @throws URISyntaxException   thrown when a URI syntax error occurs
     * @throws InterruptedException thrown when an interrupted error occurs
     */
    @SuppressWarnings("unused,UnusedReturnValue")
    public DossierList get(String onBehalfOf, APIController apiController) throws IOException, URISyntaxException, InterruptedException {
        try {
            dossiers.clear();
            Integer total = 0;
            Integer loopOffset = this.offset;

            while (loopOffset <= total) {
                Map<String, String> params = buildParams(loopOffset);

                // Process the get call
                APIResponse apiResponse = apiController.get(APIController.buildUrl(APIConstants.DOSSIERS_GET, params), onBehalfOf);

                // When the list of dossiers is returned
                if (apiResponse.getResponse().statusCode() == 200) {
                    JSONArray dossierList = apiResponse.getBody().getJSONObject("data").getJSONArray("dossiers");
                    for (Object dossier : dossierList) {
                        Dossier tmp = new Dossier(((JSONObject) dossier).getString(APIConstants.RESOURCE_UUID));
                        this.dossiers.add(tmp);
                    }

                    total = this.dossiers.isEmpty() ? -1 : apiResponse.getBody().getInt("total");
                    loopOffset += limit;
                } else {
                    logger.error("Error with code [{}] while retrieving the dossierlist", apiResponse.getResponse().statusCode());
                    total = -1;
                }
            }
        } catch (IOException | InterruptedException | URISyntaxException e) {
            logger.error("Exception occured while retrieving the dossierlist: {}", e.getMessage());
            throw e;
        }

        return this;
    }

    /**
     * Creates the parameters to use during the retrieval of the dossiers
     *
     * @param offset the start offset of the list of dossiers
     * @return the key value based list of parameters
     */
    private Map<String, String> buildParams(Integer offset) {
        Map<String, String> params = new HashMap<>();
        params.put("limit", Integer.toString(limit));
        params.put("offset", Integer.toString(offset));
        if (status != null) params.put("status", status);
        if (sort != null) params.put("sort", sort);
        if (node != null) params.put("node", node);
        if (requestTraceNr != null) params.put("requestTraceNr", requestTraceNr);
        if (resourceUuid != null) params.put("resourceUuid", resourceUuid);
        if (originalNodes != null) params.put("originalNodes", originalNodes);
        return params;
    }

    /**
     * Returns all retrieved dossiers as a list
     *
     * @return a list of the retrieved dossiers
     */
    public List<Dossier> getDossiers() {
        return dossiers;
    }

    /**
     * Sets the limit of dossiers to retrieve in a single call
     *
     * @param limit the limit, must be greater than 0 and smaller or equal to 1000
     * @return the dossier list object itself
     */
    public DossierList setLimit(Integer limit) {
        if (limit > 0 && limit <= 1000) {
            this.limit = limit;
        } else {
            logger.error("Limit should be greater than 0 and lower than or equal to 1000.");
        }
        return this;
    }

    /**
     * Sets the offset of dossiers to start retrieving of
     *
     * @param offset the offset of the list of dossiers, must be greater or equal to 0
     * @return the dossier list object itself
     */
    public DossierList setOffset(Integer offset) {
        if (offset >= 0) {
            this.offset = offset;
        } else {
            logger.error("Offset should be greater than or equal to 0.");
        }
        return this;
    }

    /**
     * Sets which status to use to retrieve dossiers with records of that status
     *
     * @param status the status of the record, can be "new"
     * @return the dossier list object itself
     */
    @SuppressWarnings("unused")
    public DossierList setStatus(String status) {
        List<String> states = List.of("new");
        if (states.contains(status) || status == null) {
            this.status = status;
        } else {
            logger.error("Status should be one of {}.", states);
        }
        return this;
    }

    /**
     * Sets the sortation of the dossiers to retrieve
     *
     * @param sort the sortation, should be one of: "creationDate", "-creationDate", "resourceUuid", "-resourceUuid"
     * @return the dossier list object itself
     */
    @SuppressWarnings("unused")
    public DossierList setSort(String sort) {
        List<String> sorts = List.of("creationDate", "-creationDate", "resourceUuid", "-resourceUuid");
        if (sorts.contains(sort) || sort == null) {
            this.sort = sort;
        } else {
            logger.error("Sort should be one of {}.", sorts);
        }

        return this;
    }

    /**
     * Sets the node of the dossiers to retrieve
     *
     * @param node a node
     * @return the dossier list object itself
     */
    @SuppressWarnings("unused")
    public DossierList setNode(String node) {
        this.node = node;
        return this;
    }

    /**
     * Sets the requesttracenr of the dossiers to retrieve
     *
     * @param requestTraceNr the requesttracenr
     * @return the dossier list object itself
     */
    @SuppressWarnings("unused")
    public DossierList setRequestTraceNr(String requestTraceNr) {
        this.requestTraceNr = requestTraceNr;
        return this;
    }

    /**
     * Sets the UUID of the dosiers to retrieve
     *
     * @param resourceUuid the UUID
     * @return the dossier list object itself
     */
    @SuppressWarnings("unused")
    public DossierList setResourceUuid(String resourceUuid) {
        this.resourceUuid = resourceUuid;
        return this;
    }

    /**
     * Sets the originalnodes of the dossiers to retrieve
     *
     * @param originalNodes the originalnode
     * @return the dossier list object itself
     */
    @SuppressWarnings("unused")
    public DossierList setOriginalNodes(String originalNodes) {
        this.originalNodes = originalNodes;
        return this;
    }
}
