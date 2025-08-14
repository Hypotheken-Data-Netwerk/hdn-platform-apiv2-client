package nl.hdn.api.object;

import nl.hdn.api.APIConstants;
import nl.hdn.api.APIController;
import nl.hdn.api.APIResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Represents a list of records on the HDN Platform of Trust
 */
public class RecordList extends APIObject {
    /**
     * A list of records
     */
    private final List<Record> records = new ArrayList<>();
    /**
     * The dossier resource UUID of the records to retieve
     */
    private final String dossierUuid;
    /**
     * The limit of dossiers to retrieve in a single call
     */
    private Integer limit = 100;
    /**
     * The offset of the list of dossier to retrieve in a single call
     */
    private Integer offset = 0;
    /**
     * The status of the records to retrieve
     */
    private String status = null;
    /**
     * The node that created the records
     */
    private String node = null;
    /**
     * The messageType of the records to retrieve
     */
    private String messageType = null;
    /**
     * The creationdate timestamp of the records to retrieve
     */
    private String creationDate = null;
    /**
     * The operator to use to compare against the creationdate
     */
    private String creationDateOperator = null;
    /**
     * The sub of the records to retrieve
     */
    private String sub = null;
    /**
     * The resource UUID of the records to retrieve
     */
    private String resourceUuid = null;
    /**
     * The sortation of the records
     */
    private String sort = null;

    /**
     * Constructs a new record list object
     */
    @SuppressWarnings("unused")
    public RecordList() {
        this.dossierUuid = null;
    }

    /**
     * Construct a new record list object, based on a dossier resource UUID
     *
     * @param dossierUuid the dossier resource UUID of the records to retrieve
     */
    @SuppressWarnings("unused")
    public RecordList(String dossierUuid) {
        this.dossierUuid = dossierUuid;
    }

    /**
     * Retrieves all records based on the parameters and filter provided
     *
     * @return the RecordList object itself
     * @throws IOException          thrown when an IO error occurs
     * @throws URISyntaxException   thrown when a URI syntax error occurs
     * @throws InterruptedException thrown when an interrupted error occurs
     * @throws JSONException        thrown when an error occurs in parsing the JSON
     */
    @SuppressWarnings("unused,UnusedReturnValue")
    public RecordList get(String onBehalfOf) throws IOException, URISyntaxException, InterruptedException, JSONException {
        return get(onBehalfOf, APIController.getInstance());
    }
    /**
     * Retrieves all records based on the parameters and filter provided
     *
     * @return the RecordList object itself
     * @throws IOException          thrown when an IO error occurs
     * @throws URISyntaxException   thrown when a URI syntax error occurs
     * @throws InterruptedException thrown when an interrupted error occurs
     * @throws JSONException        thrown when an error occurs in parsing the JSON
     */
    @SuppressWarnings("unused,UnusedReturnValue")
    public RecordList get(String onBehalfOf, APIController apiController) throws IOException, URISyntaxException, InterruptedException, JSONException {
        try {
            records.clear();
            Integer total = 0;
            Integer loopOffset = this.offset;

            while (loopOffset <= total) {
                Map<String, String> params = buildParams(loopOffset);

                // Process the get call
                String uri = this.dossierUuid == null ? APIConstants.RECORDS_GET : String.format(APIConstants.DOSSIER_GET_RECORDS, dossierUuid);
                APIResponse apiResponse = apiController.get(APIController.buildUrl(uri, params), onBehalfOf);

                // When the list of dossiers is returned
                if (apiResponse.getResponse().statusCode() == 200) {
                    JSONArray apiRecords = apiResponse.getBody().getJSONObject("data").getJSONArray("records");
                    for (Object apiRecord : apiRecords) {
                        Record tmp = new Record(((JSONObject) apiRecord).getString("dossierUuid"), ((JSONObject) apiRecord).getString(APIConstants.RESOURCE_UUID), apiRecord.toString());
                        this.records.add(tmp);
                    }

                    total = this.records.isEmpty() ? -1 : apiResponse.getBody().getInt("total");
                    loopOffset += limit;
                } else {
                    logger.error("Error with code [{}] while retrieving the recordlist", apiResponse.getResponse().statusCode());
                    total = -1;
                }
            }
        } catch (IOException | InterruptedException | URISyntaxException | JSONException e) {
            logger.error("Exception occured while retrieving the recordlist: {}", e.getMessage());
            throw e;
        }

        return this;
    }

    /**
     * Creates the parameters to use during the retrieval of the records
     *
     * @param offset the start offset of the list of records
     * @return the key value based list of parameters
     */
    private Map<String, String> buildParams(Integer offset) {
        Map<String, String> params = new HashMap<>();
        params.put("limit", Integer.toString(limit));
        params.put("offset", Integer.toString(offset));
        if (status != null) params.put("status", status);
        if (node != null) params.put("node", node);
        if (messageType != null) params.put("header.requestSchema.messageType", messageType);
        if (creationDate != null)
            params.put("creationDate" + ((creationDateOperator == null) ? "" : "[" + creationDateOperator + "]"), creationDate);
        if (sub != null) params.put("sub", sub);
        if (resourceUuid != null) params.put("resourceUuid", resourceUuid);
        if (sort != null) params.put("sort", sort);
        return params;
    }

    /**
     * Returns all retrieved records as a list
     *
     * @return a list of the retrieved records
     */
    @SuppressWarnings("unused")
    public List<Record> getRecords() {
        return records;
    }

    /**
     * Sets the limit of records to retrieve in a single call
     *
     * @param limit the limit, must be greater than 0 and smaller or equal to 1000
     * @return the record list object itself
     */
    @SuppressWarnings("unused")
    public RecordList setLimit(Integer limit) {
        if (limit > 0 && limit <= 1000) {
            this.limit = limit;
        } else {
            logger.error("Limit should be greater than 0 and lower than or equal to 1000.");
        }
        return this;
    }

    /**
     * Sets the offset of records to start retrieving of
     *
     * @param offset the offset of the list of records, must be greater or equal to 0
     * @return the record list object itself
     */
    @SuppressWarnings("unused")
    public RecordList setOffset(Integer offset) {
        if (offset >= 0) {
            this.offset = offset;
        } else {
            logger.error("Offset should be greater than or equal to 0.");
        }
        return this;
    }

    /**
     * Sets which status to use to retrieve records
     *
     * @param status the status of the record, can be "created", "sent", "new", "read", "confirmed", "rejected", "deleted"
     * @return the record list object itself
     */
    @SuppressWarnings("unused")
    public RecordList setStatus(String status) {
        List<String> states = List.of("created", "sent", "new", "read", "confirmed", "rejected", "deleted");
        if (status == null || states.contains(status)) {
            this.status = status;
        } else {
            logger.error("Status should be one of {}.", states);
        }
        return this;
    }

    /**
     * Sets the node of the records to retrieve
     *
     * @param node a node
     * @return the record list object itself
     */
    @SuppressWarnings("unused")
    public RecordList setNode(String node) {
        this.node = node;
        return this;
    }

    /**
     * Set the messageType of the records to retrieve
     *
     * @param messageType the messagetype
     * @return the record list object itself
     */
    @SuppressWarnings("unused")
    public RecordList setMessageType(String messageType) {
        List<String> messageTypes = List.of("BasisRegistratiePersonenBericht", "BronAanvraagBericht", "DesktoptaxatieBericht", "DigitaleIdentificatieBericht", "EigenaarsinformatieBericht", "EigendomsinformatieBericht", "EnergieVerbruikBericht", "HypotheekinformatieBericht", "LoondienstInkomstenBericht", "ModelmatigeWaardebepalingBericht", "ObjectBericht", "OntslagHoofdelijkeAansprakelijkheidBericht", "PensioenOverzichtBericht", "StudieLeningBericht", "TaxatieBericht", "ValidatieMelding", "VoorafIngevuldeAangifteBericht", "AX OfferteAanvraag", "CA ConsumentenBronAanvraag", "CX ConsumentenBronBericht", "DA DocumentAanvraagBericht", "DX DocumentBericht", "EA ExterneBronAanvraag", "EX ExterneBronBericht", "IA InformatieAanvraagBericht", "IX InformatieBericht", "KX KredietAanvraag", "LX LevenAanvraag", "MX BeheerVerzoek", "OX Offerte", "SX StatusMelding", "WX WaarborgBericht", "ZX InkomensverklaringOndernemerAanvraag");
        if (messageTypes.contains(messageType) || messageType == null) {
            this.messageType = messageType;
        } else {
            logger.error("MessageType should be one of {}.", messageTypes);
        }
        return this;
    }

    /**
     * Sets the filter parameters for the filtering on the creationdate
     *
     * @param operator     how to compare the creationDate, valid values are "$lt", "$lte", "$ne", "$gte", "$gt"
     * @param creationDate the creationDate to compare against
     * @return the record list object itself
     */
    @SuppressWarnings("unused")
    public RecordList setCreationDate(String operator, String creationDate) {
        List<String> operators = List.of("$lt", "$lte", "$ne", "$gte", "$gt");
        if (operators.contains(operator) || operator == null) {
            this.creationDateOperator = operator;
            this.creationDate = creationDate;
        } else {
            logger.error("Operator should be one of {}.", operators);
        }
        return this;
    }

    /**
     * Sets the sub to filter the records on
     *
     * @param sub the sub to filter
     * @return the record object list itself
     */
    @SuppressWarnings("unused")
    public RecordList setSub(String sub) {
        this.sub = sub;
        return this;
    }

    /**
     * Sets the resource UUID to filter the records on
     *
     * @param resourceUuid the UUID to filter on
     * @return the record object list itself
     */
    @SuppressWarnings("unused")
    public RecordList setResourceUuid(String resourceUuid) {
        this.resourceUuid = resourceUuid;
        return this;
    }

    /**
     * Sets the sortation of the records to retrieve
     *
     * @param sort the sortation, should be one of: "creationDate", "-creationDate", "resourceUuid", "-resourceUuid", "status", "-status", "sub", "-sub"
     * @return the record list object itself
     */
    @SuppressWarnings("unused")
    public RecordList setSort(String sort) {
        List<String> sorts = List.of("creationDate", "-creationDate", "resourceUuid", "-resourceUuid", "status", "-status", "sub", "-sub");
        if (sorts.contains(sort) || sort == null) {
            this.sort = sort;
        } else {
            logger.error("Sort should be one of {}.", sorts);
        }
        return this;
    }

    /**
     * Confirms all filtered records
     */
    @SuppressWarnings("unused")
    public void confirmAllRecords(String onBehalfOf) {
        confirmAllRecords(onBehalfOf, APIController.getInstance());
    }

    /**
     * Confirms all filtered records
     */
    @SuppressWarnings("unused")
    public void confirmAllRecords(String onBehalfOf, APIController apiController) {
        getRecords().forEach(r -> {
            try {
                logger.info("Confirming record with UUID: {}", r.getResourceUuid());
                r.fetch(onBehalfOf, apiController);
                r.confirm(onBehalfOf, apiController);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Blocks the process to wait for the receival of minimal one message with the given parameters
     *
     * @param maxRetries the max number of retries before ending the block
     * @param waitTime   the wait time between each retry
     * @return A list with the records found, an empty list indicates that the max retries have occured
     * @throws IOException          thrown when an IO error occurs
     * @throws URISyntaxException   thrown when a URI syntax error occurs
     * @throws InterruptedException thrown when an interrupted error occurs
     */
    @SuppressWarnings("unused")
    public List<Record> waitForMessage(int maxRetries, int waitTime, String onBehalfOf) throws IOException, URISyntaxException, InterruptedException {
        return waitForMessage(maxRetries, waitTime, onBehalfOf, APIController.getInstance());
    }
    /**
     * Blocks the process to wait for the receival of minimal one message with the given parameters
     *
     * @param maxRetries the max number of retries before ending the block
     * @param waitTime   the wait time between each retry
     * @return A list with the records found, an empty list indicates that the max retries have occured
     * @throws IOException          thrown when an IO error occurs
     * @throws URISyntaxException   thrown when a URI syntax error occurs
     * @throws InterruptedException thrown when an interrupted error occurs
     */
    @SuppressWarnings("unused")
    public List<Record> waitForMessage(int maxRetries, int waitTime, String onBehalfOf, APIController apiController) throws IOException, URISyntaxException, InterruptedException {
        int retryCounter = 0;
        while (retryCounter <= maxRetries) {
            List<Record> responseRecords = get(onBehalfOf, apiController).getRecords();
            if (responseRecords.isEmpty()) {
                logger.info("Message not found ({}/{})", retryCounter+1, maxRetries);
                retryCounter++;
                if (retryCounter <= maxRetries) {
                    Thread.sleep(waitTime);
                }
            } else {
                logger.info("Message found");
                return responseRecords;
            }
        }
        logger.info("Max retries reached");
        return List.of();
    }
}
