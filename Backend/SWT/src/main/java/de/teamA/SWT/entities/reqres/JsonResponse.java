package de.teamA.SWT.entities.reqres;

import org.json.JSONObject;

public class JsonResponse {

    public int status;

    public String message;

    public JsonResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public String toString() {
        JSONObject response = new JSONObject();

        try {
            response.put("status", status);
            response.put("message", message);

            return response.toString();
        } catch (Exception e) {
            return "{'status' : 500, 'message' : 'Error while parsing'";
        }

    }
}
