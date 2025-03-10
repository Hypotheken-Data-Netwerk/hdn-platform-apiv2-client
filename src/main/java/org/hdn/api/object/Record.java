package org.hdn.api.object;

import org.hdn.api.APIConstants;
import org.hdn.api.APIController;
import org.hdn.api.APIResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

/**
 * Represents a record object on the HDN Platform Of Trust
 */
public class Record extends APIObject {
    /**
     * The ExternalSource is used in a BronAanvraagBericht
     *
     * @param document The type of document you want to retrieve
     * @param provider The provider you want to use for the retrieval
     * @param source   The source that should be used by the provider
     */
    public record ExternalSource(
            String document,
            String provider,
            String source
    ) {
    }

    /**
     * The ResponseSchema is used in a BronAanvraagBericht, to indicate which schema you want to
     * use for the retrieval
     *
     * @param messageType   the messageType of the schema you want to use
     * @param receiverCode  the receiverCode of the schema you want to use
     * @param schemaVersion the schemaVersion of the schema you want to use
     * @param contentType   the contentType you want to use
     */
    public record ResponseSchema(
            String messageType,
            String receiverCode,
            String schemaVersion,
            APIConstants.ContentType contentType
    ) {
    }

    /**
     * The RequestSchema is used to indicate which schema you used for the message
     *
     * @param messageType   the messageType of the schema you want to use
     * @param schemaVersion the schemaVersion of the schema you want to use
     * @param receiverCode  the receiverCode of the schema you want to use
     * @param contentType   the contentType you want to use
     */
    public record RequestSchema(
            String messageType,
            String schemaVersion,
            String receiverCode,
            APIConstants.ContentType contentType,
            APIConstants.Environment environment
    ) {
    }

    /**
     * The header contains all relevant metadata about the message itself
     *
     * @param requestVersion  the version of the message
     * @param requestTraceNr  the tracenr of the message, is deprecated
     * @param sender          the nodenumber of the sender of the message, will be used by the receiving party for a response
     * @param receiver        the nodenumber of the receiver of the message
     * @param requestSchema   the requestSchema that has been used
     * @param responseSchemas the schemas that are expected when receiving a result, mainly used for BronAanvraagBerichten
     * @param externalSource  the externalsource to be used, mainly used for BronAanvraagBerichten
     */
    public record Header(
            String requestVersion,
            @Deprecated String requestTraceNr,
            String sender,
            String receiver,
            RequestSchema requestSchema,
            List<ResponseSchema> responseSchemas,
            ExternalSource externalSource
    ) {
    }

    /**
     * The status contains information about the status of the record
     *
     * @param value             the actual status, f.e. created, new, read, confirmed, deleted
     * @param modifiedTimestamp the Zulu timestamp when the status was last updated
     */
    public record Status(
            String value,
            Instant modifiedTimestamp
    ) {
    }

    /**
     * The SendingApplication contains information about the software that has created the
     * message on the HDN Platform of Trust
     *
     * @param applicationName    the name of the application
     * @param applicationVersion the version of the application
     * @param sendingDateTime    the Zulu time of the moment the message has been presented to the HDN Platform of Trust
     */
    public record SendingApplication(
            String applicationName,
            String applicationVersion,
            Instant sendingDateTime
    ) {
    }

    /**
     * Miscellaneous contains other relevant metadata not directly related to the message itself
     *
     * @param senderName         the business name of the sender
     * @param receiverName       the business name of the receiver
     * @param sendingApplication
     */
    public record Miscellaneous(
            String senderName,
            String receiverName,
            SendingApplication sendingApplication
    ) {
    }

    /**
     * The UUID of the record on the HDN Platform of Trust
     */
    private String resourceUuid;
    /**
     * The UUID of the node that created the message
     */
    private String sub;
    /**
     * The Zulu timestamp of the moment the record has been created
     */
    private Instant creationDate;
    /**
     * The UUID of the dossier that contains this record
     */
    private String dossierUuid;
    /**
     * The message itself
     */
    private String message;
    /**
     * The signature of the message
     */
    private byte[] messageSigned;
    /**
     * The UUID of the publickey, used to sign the message
     */
    private String publicKey;
    /**
     * The header object of the record
     */
    private Header header;
    /**
     * The miscellaneous object of the record
     */
    private Miscellaneous miscellaneous;
    /**
     * The status object of the record
     */
    private Status status;
    /**
     * The eventlist object of the record
     */
    private EventList eventList;

    /**
     * Construct a new record
     *
     * @param dossierUuid the UUID of the dossier that will contain this record
     */
    public Record(String dossierUuid) {
        this.dossierUuid = dossierUuid;
    }

    /**
     * Construct an existing record
     *
     * @param resourceUuid the UUID of the existing record
     * @param dossierUuid  the UUID of the dossier containing the record
     */
    @SuppressWarnings("unused")
    public Record(String dossierUuid, String resourceUuid) {
        this.resourceUuid = resourceUuid;
        this.dossierUuid = dossierUuid;
        eventList = new EventList(dossierUuid, resourceUuid);
    }

    /**
     * Construct an existing record
     *
     * @param resourceUuid the UUID of the existing record
     * @param dossierUuid  the UUID of the dossier containing the record
     * @param attributes   the attributes of the records in JSON format
     */
    @SuppressWarnings("unused")
    public Record(String dossierUuid, String resourceUuid, String attributes) {
        this.resourceUuid = resourceUuid;
        this.dossierUuid = dossierUuid;
        updateAttributes(new JSONObject(attributes));
    }

    /**
     * Updates the attributes, based on the JSON object returned by the platform
     *
     * @param attributes a collection of attributes returned by the platform
     */
    private void updateAttributes(JSONObject attributes) {
        JSONObject header = attributes.getJSONObject("header");
        JSONObject requestSchema = header.getJSONObject("requestSchema");
        JSONObject responseSchema = header.optJSONObject("responseSchema");
        JSONObject externalSource = header.optJSONObject("externalSource");
        JSONObject misc = attributes.getJSONObject("miscellaneous");
        JSONObject sa = misc.getJSONObject("sendingApplication");
        JSONObject status = attributes.getJSONObject("status");

        sub = attributes.getString("sub");
        this.miscellaneous = new Miscellaneous(misc.getString("senderName"), misc.getString("receiverName"), new SendingApplication(sa.getString("applicationName"), sa.getString("applicationVersion"), Instant.parse(sa.getString("sendingDateTime"))));
        this.header = new Header(
                header.getString("requestVersion"),
                header.getString("requestTraceNr"),
                header.getString("sender"),
                header.getString("receiver"),
                new RequestSchema(
                        requestSchema.getString("messageType"),
                        requestSchema.getString("schemaVersion"),
                        requestSchema.getString("receiverCode"),
                        APIConstants.ContentType.valueOf(requestSchema.getString("contentType")),
                        APIConstants.Environment.valueOf(requestSchema.getString("environment"))
                ),
                responseSchema != null ? List.of(new ResponseSchema(
                        responseSchema.getString("messageType"),
                        responseSchema.getString("receiverCode"),
                        responseSchema.getString("schemaVersion"),
                        APIConstants.ContentType.valueOf(requestSchema.getString("contentType"))
                )) : List.of(),
                externalSource != null ? new ExternalSource(
                        externalSource.getString("document"),
                        externalSource.getString("provider"),
                        externalSource.getString("source")
                ) : null
        );
        creationDate = Instant.parse(attributes.getString("creationDate"));
        resourceUuid = attributes.getString("resourceUuid");
        this.status = new Status(status.getString("value"), Instant.parse(status.getString("modifiedTimestamp")));
        dossierUuid = attributes.getString("dossierUuid");
        eventList = new EventList(dossierUuid, resourceUuid);
    }

    /**
     * Sets the sub of this record
     *
     * @param sub the new sub
     * @return the record object itself
     */
    @SuppressWarnings("unused")
    public Record setSub(String sub) {
        this.sub = sub;
        return this;
    }

    /**
     * Sets the UUID of the publickey used to sign the message
     *
     * @param publicKey the UUID of the publickey
     * @return the record object itself
     */
    @SuppressWarnings("unused")
    public Record setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    /**
     * Sets the miscellaneous object of the record
     *
     * @param miscellaneous the miscellaneous object
     * @return the record object itself
     */
    @SuppressWarnings("unused")
    public Record setMiscellaneous(Miscellaneous miscellaneous) {
        this.miscellaneous = miscellaneous;
        return this;
    }

    /**
     * Sets the header object of the record
     *
     * @param header the header object
     * @return the record object itself
     */
    @SuppressWarnings("unused")
    public Record setHeader(Header header) {
        this.header = header;
        return this;
    }

    /**
     * Sets the message content of the record
     *
     * @param message the message
     * @return the record object itself
     */
    @SuppressWarnings("unused")
    public Record setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Fetches all data of the record from the HDN Platform of Trust
     *
     * @return the record object itself
     * @throws IOException          exception thrown when an IO error has occured
     * @throws InterruptedException exception thrown when the API request to the platform was interrupted
     */
    @SuppressWarnings("unused,UnusedReturnValue")
    public Record fetch() throws IOException, InterruptedException {
        APIResponse APIResponse = APIController.getInstance().get(String.format(APIConstants.DOSSIER_GET_RECORD, dossierUuid, resourceUuid));

        if (APIResponse.getResponse().statusCode() == 200) {
            updateAttributes(APIResponse.getBody());
            message = new String(Base64.getDecoder().decode(APIResponse.getBody().getJSONObject("message").getString("data")));
        }
        return this;
    }

    /**
     * Creates the record on the HDN Platform of Trust when the record has not been created yet
     *
     * @return When a record is non-existing the result of the response of the platform, otherwise null
     * @throws IOException          exception thrown when an IO error has occured
     * @throws InterruptedException exception thrown when the API request to the platform was interrupted
     */
    @SuppressWarnings("unused")
    public APIResponse create() throws IOException, InterruptedException {
        if (resourceUuid == null) {
            JSONObject body = new JSONObject();
            JSONArray responseSchemas = new JSONArray();

            body.put("header", new JSONObject()
                    .put("receiver", header.receiver)
                    .put("requestVersion", header.requestVersion)
                    .put("requestSchema", new JSONObject()
                            .put("messageType", header.requestSchema.messageType)
                            .put("receiverCode", header.requestSchema.receiverCode)
                            .put("schemaVersion", header.requestSchema.schemaVersion)
                            .put("contentType", header.requestSchema.contentType)
                            .put("environment", header.requestSchema.environment)
                    )
            );

            if (!header.responseSchemas.isEmpty()) {
                header.responseSchemas.forEach(responseSchema -> responseSchemas.put(new JSONObject()
                        .put("messageType", responseSchema.messageType)
                        .put("receiverCode", responseSchema.receiverCode)
                        .put("schemaVersion", responseSchema.schemaVersion)
                        .put("contentType", responseSchema.contentType)
                ));
                body.getJSONObject("header").put("responseSchemas", responseSchemas);
            }

            if (header.externalSource != null) {
                body.getJSONObject("header").put("externalSource", new JSONObject()
                        .put("document", header.externalSource.document)
                        .put("provider", header.externalSource.provider)
                        .put("source", header.externalSource.source)
                );
            }

            body.put("message", new JSONObject()
                    .put("data", Base64.getEncoder().encodeToString(message.getBytes()))
                    .put("signature", new JSONObject()
                            .put("publicKey", new JSONObject()
                                    .put("uuid", publicKey))
                            .put("value", Base64.getEncoder().encodeToString(messageSigned))));

            body.put("miscellaneous", new JSONObject()
                    .put("senderName", miscellaneous.senderName)
                    .put("receiverName", miscellaneous.receiverName)
                    .put("sendingApplication", new JSONObject()
                            .put("applicationName", miscellaneous.sendingApplication.applicationName)
                            .put("applicationVersion", miscellaneous.sendingApplication.applicationVersion)
                            .put("sendingDateTime", miscellaneous.sendingApplication.sendingDateTime)));

            APIResponse APIResponse = APIController.getInstance().post(String.format(APIConstants.DOSSIER_CREATE_RECORD, dossierUuid), body.toString());

            // When a record is created
            if (APIResponse.getResponse().statusCode() == 201) {
                // Update the attributes
                updateAttributes(APIResponse.getBody());
            } else {
                logger.error(APIResponse.getResponse().body());
            }
            return APIResponse;
        }
        return null;
    }

    /**
     * Sends a record that has been created
     *
     * @return When a record exists the result of the response of the platform, otherwise null
     * @throws IOException          exception thrown when an IO error has occured
     * @throws InterruptedException exception thrown when the API request to the platform was interrupted
     */
    @SuppressWarnings("unused")
    public APIResponse send() throws IOException, InterruptedException {
        if (resourceUuid != null) {
            return APIController.getInstance().post(String.format(APIConstants.DOSSIER_SEND_RECORD, dossierUuid, resourceUuid));
        }
        logger.error("Couldn't send record, because record is not created yet!");
        return null;
    }

    /**
     * Signs the message with the configured key
     *
     * @return the record object itself
     * @throws IOException               exception thrown when an IO error has occured
     * @throws KeyStoreException         exception thrown when a keystore error has occured
     * @throws CertificateException      exception thrown when a certificate error has occured
     * @throws NoSuchAlgorithmException  exception thrown when an algorithm error has occured
     * @throws UnrecoverableKeyException exception thrown when an unreceoverable key error has ooccured
     * @throws InvalidKeyException       exception thrown when an invalid key error has occured
     * @throws SignatureException        exception thrown when a signature error has occured
     */
    @SuppressWarnings("unused")
    public Record signMessage() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException {
        PrivateKey pk = APIController.getInstance().getPrivateKey();
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(pk);
        signature.update(message.getBytes(StandardCharsets.UTF_8));
        messageSigned = signature.sign();

        return this;
    }

    /**
     * Confirms the record on the HDN Platform of Trust to indicate the record has been processed by the receiver
     *
     * @return When a record exists the result of the response of the platform, otherwise null
     * @throws IOException          exception thrown when an IO error has occured
     * @throws InterruptedException exception thrown when the API request to the platform was interrupted
     */
    @SuppressWarnings("unused,UnusedReturnValue")
    public APIResponse confirm() throws IOException, InterruptedException {
        if (resourceUuid != null) {
            return APIController.getInstance().post(String.format(APIConstants.DOSSIER_CONFIRM_RECORD, dossierUuid, resourceUuid));
        }
        logger.error("Couldn't confirm record, because record is not created yet!");
        return null;
    }

    /**
     * Returns the sub of the record
     *
     * @return the sub itself
     */
    @SuppressWarnings("unused")
    public String getSub() {
        return sub;
    }

    /**
     * Returns the creation date of the record
     *
     * @return the creation date
     */
    @SuppressWarnings("unused")
    public Instant getCreationDate() {
        return creationDate;
    }

    /**
     * Returns the message of the record after a fetch() call has been executed, otherwise null will be returned
     *
     * @return the message
     */
    @SuppressWarnings("unused")
    public String getMessage() {
        return message;
    }

    /**
     * Returns the header object of the record
     *
     * @return the header object
     */
    @SuppressWarnings("unused")
    public Header getHeader() {
        return header;
    }

    /**
     * Returns the miscellaneous object of the record
     *
     * @return the miscellaneous object
     */
    @SuppressWarnings("unused")
    public Miscellaneous getMiscellaneous() {
        return miscellaneous;
    }

    /**
     * Returns the status of the record
     *
     * @return the status
     */
    @SuppressWarnings("unused")
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the UUID of the record
     *
     * @return the UUID
     */
    @SuppressWarnings("unused")
    public String getResourceUuid() {
        return resourceUuid;
    }

    /**
     * Returns the EventList of the record
     *
     * @return the eventList object
     */
    @SuppressWarnings("unused")
    public EventList getEventList() {
        return eventList;
    }
}
