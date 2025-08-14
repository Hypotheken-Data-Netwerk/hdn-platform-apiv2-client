package nl.hdn.api;

import org.json.JSONObject;

import java.net.http.HttpResponse;

/**
 * Represents the response of the API
 */
public class APIResponse {
    /**
     * The original response of the API
     */
    private final HttpResponse<String> response;
    /**
     * The parsed JSON part of the response, if any
     */
    private final JSONObject body;

    /**
     * Construct the APIResponse object
     * @param response the original response
     */
    public APIResponse(HttpResponse<String> response) {
        this.response = response;
        
        if (response.headers().firstValue("Content-Type").orElse("").contains("application/json")) {
            body = new JSONObject(response.body());
        } else {
            body = null;
        }
    }

    /**
     * Returns the original response
     * @return the original HTTP response
     */
    @SuppressWarnings("unused")
    public HttpResponse<String> getResponse() {
        return response;
    }

    /**
     * Checks if a JSON was present in the response
     * @return true when JSON was returned, false otherwise
     */
    @SuppressWarnings("unused")
    public boolean hasJSONBody() {
        return body != null;
    }

    /**
     * Returns the parsed JSON object of the response, if any, otherwise null
     * @return the parsed JSON object of the response or null
     */
    @SuppressWarnings("unused")
    public JSONObject getBody() {
        return body;
    }
}
