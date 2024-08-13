package Handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.services.ServiceException;
import com.amazon.ask.model.services.deviceAddress.Address;
import com.amazon.ask.model.services.deviceAddress.DeviceAddressServiceClient;
import com.amazon.ask.request.Predicates;
import com.amazon.ask.response.ResponseBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.alexis.TransitApiService;

import java.io.IOException;
import java.util.*;

public class SearchStopsIntentHandler implements IntentRequestHandler {

    private static final OkHttpClient client = new OkHttpClient();
    private static final String apiKey = System.getenv("geoAPI");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    final static HashMap<Integer, HashMap<String, String>> stops = new HashMap<>(); //Keys are integers and values are hashmaps where the key is a stop id and value an address of such stop
    private static Address address;
    private boolean hasPermissions;
    private HandlerInput input;

    @Override
    public boolean canHandle(HandlerInput handlerInput, IntentRequest intentRequest) {
        String deviceID = handlerInput.getRequestEnvelope().getContext().getSystem().getDevice().getDeviceId();
        //getServiceClientFactory returns a service client instance to have access to Alexa APIs. In this case it's the Device settings API
        DeviceAddressServiceClient deviceAddressService = handlerInput.getServiceClientFactory().getDeviceAddressService();
        int statusCode = 200;
        try {
            address = deviceAddressService.callGetFullAddress(deviceID).getResponse();
        } catch (ServiceException e) {
            statusCode = e.getStatusCode();

        }
        hasPermissions = statusCode == 200; // if skill has permissions to access device address.
        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> sessionAttributes = attributesManager.getSessionAttributes();
        sessionAttributes.put("stops", stops); //sets the stops hashmap to a session attribute
        attributesManager.setSessionAttributes(sessionAttributes);
        input = handlerInput;
        return handlerInput.matches(Predicates.intentName("SearchStopsIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        try {
            if (address != null && !address.getStateOrRegion().equalsIgnoreCase("NJ")) {
                return getStateResponse();
            }
        } catch (NullPointerException ignored){}
        if (hasPermissions) {
            setNearbyStops();
            return getNearbyStops();
        } else {
            return allowPermissionsResponse();
        }
    }

    public Optional<Response> getStateResponse() {
        String speechText = "Sorry, Transit Tracker is only available for the state of New Jersey at the moment. " +
                "Thank you for your interest!";
        return input.getResponseBuilder().withSpeech(speechText).build();
    }

    private Optional<Response> allowPermissionsResponse() {
        ResponseBuilder responseBuilder = new ResponseBuilder();
        ArrayList<String> permissions = new ArrayList<>(List.of("read::alexa:device:all:address"));
        String message = "I'll need access to your device location for searching stops near you. " +
                "Please go to the Alexa App to allow this skill access to your location, and try again.";
        return responseBuilder.withSpeech(message).withAskForPermissionsConsentCard(permissions).build();
    }

    private Optional<Response> getNearbyStops() {
        AttributesManager attributesManager = input.getAttributesManager();
        Map<String, Object> sessionAttributes = attributesManager.getSessionAttributes();
        HashMap<Integer, HashMap<String, String>> retrievedStops = (HashMap<Integer, HashMap<String, String>>) sessionAttributes.get("stops");
        ResponseBuilder responseBuilder = new ResponseBuilder();
        int index = 1; //index variable helps access stops by their Integer key
        if (retrievedStops.isEmpty()) {
            return responseBuilder.withSpeech("Sorry, but there are no stops nearby. If you would like to" +
                            " check a specific stop then say, search stop by code.")
                    .withShouldEndSession(false).build();
        } else if (retrievedStops.size() == 1) {
            return responseBuilder.withSpeech("I have found a stop nearby. This stop is located at " + retrievedStops.get(index).values().iterator().next()).build();
        } else {
            StringBuilder speechText = new StringBuilder("I've found a list of stops nearby. ");
            for (Integer ignored : retrievedStops.keySet()) {
                speechText.append("Stop number " + index + " is located at " + retrievedStops.get(index).values().iterator().next() + ". ");
                index++;
            }
            speechText.append("You can say, check stop number 1, set stop number 1 as default, or repeat stops.");
            return responseBuilder.withSpeech(speechText.toString()).withShouldEndSession(false).build();
        }
    }

    private void setNearbyStops() {
        AttributesManager attributesManager = input.getAttributesManager();
        Map<String, Object> sessionAttributes = attributesManager.getSessionAttributes();
        HashMap<Integer, HashMap<String, String>> stops = (HashMap<Integer, HashMap<String, String>>) sessionAttributes.get("stops");
        try {
            JsonNode rootNode = objectMapper.readTree(TransitApiService.getNearbyStopsResponse());
            JsonNode arrayNode = rootNode.get("stops"); //array of objects representing a stop

            int index = 1;
            for (JsonNode node : arrayNode) {
                String globalStopId = node.get("global_stop_id").asText();
                double lat = node.get("stop_lat").asDouble();
                double lon = node.get("stop_lon").asDouble();
                HashMap<String, String> map = new HashMap<>();
                map.put(globalStopId, getReverseGeolocation(lat,lon));
                stops.put(index, map);
                index++;
            }
        } catch (JsonProcessingException | NullPointerException exception) {
//            throw new RuntimeException("Error processing JSON response.", exception);
        }
        sessionAttributes.put("stops", stops); //adds back the stops hashmap with a populated hashmap (stops)
        attributesManager.setSessionAttributes(sessionAttributes);
    }


    private String getReverseGeolocation(double lat, double lon) {
        String jsonResponse;
        Request request = new Request.Builder()
                .url("https://api.geocodify.com/v2/reverse?api_key=" + apiKey +
                        "&lat=" + lat + "&lng=" + lon)
                .build();
        try {
            jsonResponse = client.newCall(request).execute().body().string();
            //access the name attribute to return the address of coordinates passed as parameters.
            return objectMapper.readTree(jsonResponse).get("response").get("features").get(0).
                    get("properties").get("name").asText();
        } catch (IOException | NullPointerException exception) {
//            throw new RuntimeException();
            return "Exception!";
        }
    }

    private static String getDeviceAddress() {
        try {
            return (address.getAddressLine1() != null ? address.getAddressLine1() : "") + " " +
                    (address.getAddressLine2() != null ? address.getAddressLine2() : "") + " " +
                    (address.getAddressLine3() != null ? address.getAddressLine3() : "") + " " +
                    (address.getCity() != null ? address.getCity() : "") + " " +
                    (address.getStateOrRegion() != null ? address.getStateOrRegion() : "");
        } catch (ServiceException exception) {
            return "exception!";
        }
    }


    private static String getGeolocationResponse() {
        Request request = new Request.Builder()
                .url("https://api.geocodify.com/v2/geocode?api_key=" + apiKey +
                        "&q=" + getDeviceAddress())
                .build();
        try {
            return client.newCall(request).execute().body().string();
        } catch (IOException exception){
            return "Exception!";
        }
    }

    public static String getLatLong() {
        try {
            JsonNode jsonNode = objectMapper.readTree(getGeolocationResponse());
            JsonNode coordinate = jsonNode.get("response").get("features").get(0).get("geometry").get("coordinates");
            return "lat=" + coordinate.get(1).asText() + "&lon=" + coordinate.get(0).asText();
        } catch (NullPointerException | JsonProcessingException exception) {
//            throw new RuntimeException();
            return "Exception!";
        }
    }
}
